(function ($) {
    'use strict';

    /*** 상수/초기값 ***/
    const $body = $('body');
    const ROLE = String($body.data('role') || 'MANAGER').toUpperCase();
    const IS_MANAGER = ROLE === 'MANAGER';
    const PAGE_SIZE = parseInt($body.data('pageSize') || '10', 10);
    const VISIBLE_PAGES = 5; // 번호 5개 고정

    // 직영점 화면은 body에 data-store-no 필수
    const STORE_NO = !IS_MANAGER ? parseInt($body.data('storeNo') || '0', 10) : null;

    // 본사 화면에서 직영점 선택 상태
    let selectedStoreNo = null;
    let selectedStoreName = '';

    // 모달 인스턴스
    let limitModalInstance = null;
    let disposeModalInstance = null;

    /*** 오버레이 토글(본사) ***/
    function updateOverlay() {
        const $ov = $('#storeNoticeOverlay');
        if (!$ov.length) return;
        const hasStore = !!selectedStoreNo || $.trim($('#storeSearchKeyword').val() || '') !== '';
        $ov.toggleClass('d-none', hasStore).toggleClass('d-flex', !hasStore);
    }

    /*** 공통 유틸 ***/
    function normalizeSearchType(v) {
        if (!v) return null;
        const t = String(v).toUpperCase();
        if (t === 'ITEM_NAME') return 'NAME';
        if (t === 'ITEM_CODE') return 'CODE';
        if (t === 'NAME' || t === 'CODE') return t;
        return null;
    }

    const STATE_KEY = 'storeItemState:' + location.pathname;

    function collectState() {
        return {
            role: ROLE,
            storeNo: IS_MANAGER ? (selectedStoreNo || '') : (STORE_NO || ''),
            storeName: IS_MANAGER ? (selectedStoreName || '') : '',
            category: $('#categorySelect').val() || '',
            searchType: $('#searchTypeSelect').val() || 'ITEM_NAME',
            keyword: ($('#searchKeyword').val() || '').trim(),
            page: window._currentPage || 1
        };
    }
    function applyState(st) {
        if (!st) return;
        $('#categorySelect').val(st.category ?? '');
        $('#searchTypeSelect').val(st.searchType ?? 'ITEM_NAME');
        $('#searchKeyword').val(st.keyword ?? '');
        if (IS_MANAGER) {
            selectedStoreNo = st.storeNo ? parseInt(st.storeNo, 10) : null;
            selectedStoreName = st.storeName || '';
            if (selectedStoreName) $('#storeSearchKeyword').val(selectedStoreName);
        }
        updateOverlay();
    }
    function pushUrl(st) {
        const u = new URL(location.href);
        u.searchParams.set('page', st.page);
        st.category ? u.searchParams.set('category', st.category) : u.searchParams.delete('category');
        st.searchType ? u.searchParams.set('searchType', st.searchType) : u.searchParams.delete('searchType');
        st.keyword ? u.searchParams.set('keyword', st.keyword) : u.searchParams.delete('keyword');
        st.storeNo ? u.searchParams.set('storeNo', st.storeNo) : u.searchParams.delete('storeNo');
        st.storeName ? u.searchParams.set('storeName', st.storeName) : u.searchParams.delete('storeName');
        history.replaceState(st, '', u);
    }
    function readUrl() {
        const p = new URLSearchParams(location.search);
        return {
            storeNo: p.get('storeNo') || '',
            storeName: p.get('storeName') || '',
            category: p.get('category') || '',
            searchType: p.get('searchType') || 'ITEM_NAME',
            keyword: p.get('keyword') || '',
            page: parseInt(p.get('page') || '1', 10)
        };
    }

    /*** 현재재고 < 하한선 강조 ***/
    function markLowRows() {
        $('#stockTableBody tr').each(function () {
            const cur = numOrNull($(this).data('currentqty')) ?? 0;
            const lim = numOrNull($(this).data('limit'));
            const $cell = $(this).find('.cell-current');
            if (lim != null && cur < lim) $cell.addClass('text-danger fw-bold');
            else $cell.removeClass('text-danger fw-bold');
        });
    }

    /*** Toast 유틸 ***/
    function toast(msg, type = 'success') {
        let box = document.getElementById('toastBox');
        if (!box) {
            box = document.createElement('div');
            box.id = 'toastBox';
            box.style.position = 'fixed';
            box.style.right = '16px';
            box.style.bottom = '16px';
            box.style.zIndex = '9999';
            document.body.appendChild(box);
        }
        const el = document.createElement('div');
        el.className = `alert alert-${type} shadow-sm py-2 px-3 mb-2`;
        el.style.minWidth = '220px';
        el.textContent = msg;
        box.appendChild(el);
        setTimeout(() => {
            el.style.transition = 'opacity .2s';
            el.style.opacity = '0';
            setTimeout(() => el.remove(), 200);
        }, 1800);
    }
    window.toast = toast;

    /*** CSRF 메타 → 헤더 ***/
    function getCsrf() {
        const t = document.querySelector('meta[name="_csrf"]');
        const h = document.querySelector('meta[name="_csrf_header"]');
        return (t && h) ? { token: t.content, header: h.content } : null;
    }

    /*** 숫자 파서 ***/
    function numOrNull(v) {
        if (v === '' || v === undefined || v === null) return null;
        const n = Number(v);
        return Number.isFinite(n) ? n : null;
    }

    /*** 하한선 모달: 환산/단위 헬퍼 ***/
    function getConvFromRow($row) {
        const conv = Number($row?.data('convertstock'));
        return Number.isFinite(conv) && conv > 0 ? conv : null;
    }
    function getUnitsFromRow($row) {
        const stockUnit = String($row?.data('unit') || '').trim() || 'ea';
        const supplyUnit = String($row?.data('supplyunit') || '').trim() || 'box';
        return { stockUnit, supplyUnit };
    }

    /*** 하한선 입력 검증 ***/
    function validateLimitInput() {
        const val = $('#limitNew').val();
        const $err = $('#limitErrorText');
        const $btn = $('#btnSaveLimit');
        if (val === '') { $err.addClass('d-none'); $btn.prop('disabled', false); return; }
        const num = Number(val);
        if (!Number.isFinite(num) || num <= 0) { $err.removeClass('d-none'); $btn.prop('disabled', true); }
        else { $err.addClass('d-none'); $btn.prop('disabled', false); }
    }
    $(document).on('input', '#limitNew', validateLimitInput);

    // 공급단위 계산
    $(document).on('change', '#limitUseSupply', function () {
        const on = this.checked;
        $('#limitSupplyCount').prop('disabled', !on);
        $('#limitErrorText').addClass('d-none');
        if (!on) $('#limitSupplyCount').val(''); else $('#limitSupplyCount').trigger('input');
    });
    $(document).on('input', '#limitSupplyCount', function () {
        const boxes = parseFloat(this.value) || 0;
        theConstFix();
        const conv = Number($('#limitModal').data('convertStock') || 0);
        if (conv > 0 && boxes >= 0) $('#limitNew').val(boxes * conv).trigger('input');
    });

    // === 소유자 라벨/입력 토글 유틸 추가 ===
    function setOwnerLabel(owner) {
        const $o = $('#limitOwnerText');
        $o.removeClass('text-muted text-secondary text-danger text-success');
        if (owner === 'MANAGER') {
            $o.text('본사에서 설정한 하한선입니다.').addClass('text-danger'); // 빨강
        } else if (owner === 'STORE') {
            $o.text('직영점에서 설정한 하한선입니다.').addClass('text-success'); // 초록
        } else {
            $o.text('현재 설정된 하한선이 없습니다.').addClass('text-secondary'); // 회색
        }
    }
    function setLimitInputsEnabled(enabled) {
        $('#limitNew').prop('disabled', !enabled);
        $('#limitUseSupply').prop('disabled', !enabled);
        const useSupply = $('#limitUseSupply').is(':checked');
        $('#limitSupplyCount').prop('disabled', !enabled || !useSupply);
        $('#btnSaveLimit').prop('disabled', !enabled);
        if (!enabled) {
            $('#limitErrorText').addClass('d-none');
            $('#limitNew').attr('placeholder', '본사 설정 수정불가');
        } else {
            $('#limitNew').attr('placeholder', '');
        }
    }

    /*** 하한선 모달 열기 ***/
    function openLimitModalByRow($row) {
        if (!$row || !$row.length) return;

        const storeName = $row.data('storename') || '';
        const itemName = $row.data('itemname') || '';
        const rawLimit = $row.data('limit');
        const owner = (String($row.data('owner') || 'NONE')).toUpperCase();
        const storeItemNo = $row.data('storeitemno');

        const conv = getConvFromRow($row);
        const { stockUnit, supplyUnit } = getUnitsFromRow($row);

        $('#limitStoreName').text(IS_MANAGER ? storeName : '');
        $('#limitItemName').text(itemName);

        if (rawLimit === undefined || rawLimit === null || rawLimit === '') {
            $('#limitCurrentText').text('설정 없음');
        } else {
            $('#limitCurrentText').text(Number(rawLimit).toLocaleString() + ' ' + stockUnit);
        }

        // 소유자 문구 + 색상
        setOwnerLabel(owner);

        // 입력 가능 여부: 직영점 화면에서 본사 설정이면 금지
        if (!IS_MANAGER && owner === 'MANAGER') setLimitInputsEnabled(false);
        else setLimitInputsEnabled(true);

        $('#limitNew').val('');
        $('#limitUnit2').text(stockUnit);
        $('#limitUseSupply').prop('checked', false);
        $('#limitSupplyCount').prop('disabled', true).val('');
        $('#limitSupplyUnit').text(supplyUnit);
        $('#limitConvertInfo').text(
            conv ? `※ 1 ${supplyUnit} = ${conv} ${stockUnit}` : '※ 공급단위-재고단위 변환 정보를 불러올 수 없습니다.'
        );

        $('#limitErrorText').addClass('d-none');

        // 모달 메타
        $('#limitModal').data('storeItemNo', storeItemNo);
        $('#limitModal').data('convertStock', conv || 0);
        $('#limitModal').data('supplyUnit', supplyUnit);
        $('#limitModal').data('stockUnit', stockUnit);
        $('#limitModal').data('owner', owner); // 저장 가드용

        if (!limitModalInstance) {
            const el = document.getElementById('limitModal');
            if (!el) return;
            limitModalInstance = new bootstrap.Modal(el);
        }
        limitModalInstance.show();
    }
    $(document).on('click', '.btn-open-limit', function () {
        openLimitModalByRow($(this).closest('tr'));
    });
    window.openLimitModal = function (el) { openLimitModalByRow($(el).closest('tr')); };

    /*** 행 데이터(한도/소유자/셀) 즉시 반영 유틸 ***/
    function applyLimitToRow($row, finalLimit, owner, unit) {
        // data-* 갱신
        $row.data('limit', finalLimit != null ? finalLimit : '');
        $row.data('owner', owner || 'NONE');

        // 표 셀 텍스트 갱신
        const text = (finalLimit == null) ? '-' : (Number(finalLimit).toLocaleString() + ' ' + (unit || 'ea'));
        $row.find('.cell-limit').contents().filter(function () {
            return this.nodeType === Node.TEXT_NODE;
        }).first().replaceWith(text + ' ');

        // 재고<하한선 강조 재계산
        markLowRows();
    }

    /*** 하한선 저장 (CSRF 헤더 주입 + 즉시 fallback 반영) ***/
    $(document).on('click', '#btnSaveLimit', function () {
        const $modal = $('#limitModal');
        const storeItemNo = $modal.data('storeItemNo');
        if (!storeItemNo) { toast('선택된 품목 정보가 없습니다.', 'warning'); return; }

        // 직영점 화면 + 본사 설정이면 저장 금지 (이중 방어)
        const ownerNow = String($modal.data('owner') || 'NONE').toUpperCase();
        if (!IS_MANAGER && ownerNow === 'MANAGER') {
            toast('본사 설정 중에는 직영점에서 수정할 수 없습니다.', 'warning');
            return;
        }

        const val = $('#limitNew').val().trim();
        let newLimit = null;
        if (val !== '') {
            const num = Number(val);
            if (!Number.isFinite(num) || num <= 0) { $('#limitErrorText').removeClass('d-none'); return; }
            newLimit = num;
        }

        const params = new URLSearchParams();
        if (newLimit !== null) params.append('newLimit', newLimit);
        params.append('isManagerRole', IS_MANAGER ? 'true' : 'false');

        const csrf = getCsrf();
        const headers = { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' };
        if (csrf) headers[csrf.header] = csrf.token;

        fetch(`/stock/storeItem/${storeItemNo}/limit`, {
            method: 'POST',
            headers,
            body: params.toString(),
            credentials: 'same-origin'
        })
            .then(res => {
                if (!res.ok) throw new Error('limit save failed');

                // 저장 성공 → 행 즉시 반영 (fallback 포함)
                const $row = $('#stockTableBody tr').filter(function () {
                    return String($(this).data('storeitemno')) === String(storeItemNo);
                });
                if ($row.length) {
                    const unit = ($row.data('unit') || 'ea');

                    // 현재 행의 두 한도값 읽기
                    let man = numOrNull($row.data('managerlimit'));
                    let sto = numOrNull($row.data('storelimit'));

                    if (IS_MANAGER) {
                        // 본사에서 저장 → managerLimit 갱신
                        man = (newLimit == null) ? null : Number(newLimit);
                        $row.data('managerlimit', man != null ? man : '');
                        const final = (man != null) ? man : (sto != null ? sto : null);
                        const owner = (man != null) ? 'MANAGER' : (sto != null ? 'STORE' : 'NONE');
                        applyLimitToRow($row, final, owner, unit);
                    } else {
                        // 직영점에서 저장 → storeLimit 갱신
                        sto = (newLimit == null) ? null : Number(newLimit);
                        $row.data('storelimit', sto != null ? sto : '');
                        const final = (man != null) ? man : (sto != null ? sto : null); // 본사 우선
                        const owner = (man != null) ? 'MANAGER' : (sto != null ? 'STORE' : 'NONE');
                        applyLimitToRow($row, final, owner, unit);
                    }
                }

                toast('하한선을 저장했습니다.', 'success');
                if (limitModalInstance) limitModalInstance.hide();
            })
            .catch(err => {
                console.error(err);
                toast('하한선 저장에 실패했습니다.', 'danger');
            });
    });

    /*** 직영점 전용: 폐기 모달 ***/
    function openDisposeModal($row) {
        if (IS_MANAGER) return;
        if (!$row || !$row.length) return;

        const itemName = $row.data('itemname') || '';
        const currentQty = Number($row.data('currentqty')) || 0;
        const unit = $row.data('unit') || 'ea';

        $('#disposeItemName').text(itemName);
        $('#disposeCurrentQty').text(currentQty.toLocaleString());
        $('#disposeUnit, #disposeUnit2').text(unit);
        $('#disposeQtyInput').val('');
        $('#checkSupplyDispose').prop('checked', false);
        $('#disposeSupplyCount').val('').prop('disabled', true);

        const conv = getConvFromRow($row);
        const { supplyUnit, stockUnit } = getUnitsFromRow($row);
        $('#disposeSupplyUnit').text(supplyUnit);
        $('#disposeConvertInfo').text(
            conv ? `※ 1 ${supplyUnit} = ${conv} ${stockUnit}` : '※ 공급단위-재고단위 변환 정보를 불러올 수 없습니다.'
        );

        const modal = $('#disposeModal');
        modal.data('convertStock', conv || 0);
        modal.data('supplyUnit', supplyUnit);
        modal.data('stockUnit', stockUnit);

        if (!disposeModalInstance) {
            const el = document.getElementById('disposeModal');
            if (!el) { toast('폐기 모달이 정의되어 있지 않습니다.', 'warning'); return; }
            disposeModalInstance = new bootstrap.Modal(el);
        }
        disposeModalInstance.show();
    }
    $(document).on('click', '.btn-open-dispose', function () {
        if (!IS_MANAGER) openDisposeModal($(this).closest('tr'));
    });
    $(document).on('change', '#checkSupplyDispose', function () {
        const on = $(this).is(':checked');
        $('#disposeSupplyCount').prop('disabled', !on);
        if (!on) $('#disposeSupplyCount').val('');
        else {
            const conv = parseInt($('#disposeModal').data('convertStock') || '0', 10);
            const sc = parseInt($('#disposeSupplyCount').val() || '0', 10);
            if (conv > 0 && sc >= 0) $('#disposeQtyInput').val(sc * conv);
        }
    });
    $(document).on('input', '#disposeSupplyCount', function () {
        const conv = parseInt($('#disposeModal').data('convertStock') || '0', 10);
        const sc = parseInt($(this).val() || '0', 10);
        if (conv > 0 && sc >= 0) $('#disposeQtyInput').val(sc * conv);
    });

    /*** 본문 테이블 렌더 (두 한도 data-* 포함) ***/
    function buildRowHtml(item) {
        const limitVal = item.limitQuantity ?? item.finalLimit ?? null;
        const limitText = (limitVal != null)
            ? (Number(limitVal).toLocaleString() + ' ' + (item.stockUnit || 'ea'))
            : '-';
        const currentText = (item.currentQuantity || 0).toLocaleString() + ' ' + (item.stockUnit || 'ea');

        // 새 필드(선택): managerLimit, storeLimit
        const managerLimit = (item.managerLimit ?? null);
        const storeLimit   = (item.storeLimit ?? null);
        const ownerAttr    = (item.limitOwnerType || item.limitOwner || 'NONE');

        if (IS_MANAGER) {
            return `
<tr
  data-storeitemno="${item.storeItemNo}"
  data-storename="${item.storeName || ''}"
  data-itemname="${item.itemName || ''}"
  data-category="${item.itemCategory || ''}"
  data-currentqty="${item.currentQuantity || 0}"
  data-unit="${item.stockUnit || 'ea'}"
  data-convertstock="${item.convertStock ?? ''}"
  data-supplyunit="${item.supplyUnit || 'box'}"
  data-limit="${limitVal != null ? limitVal : ''}"
  data-owner="${ownerAttr}"
  data-managerlimit="${managerLimit ?? ''}"
  data-storelimit="${storeLimit ?? ''}"
>
  <td>${item.storeName || ''}</td>
  <td class="text-code">${item.itemCode || ''}</td>
  <td>${item.itemName || ''}</td>
  <td>${item.itemCategory || ''}</td>
  <td class="text-qty cell-limit">
    ${limitText}
    <button type="button" class="icon-btn btn-open-limit" title="하한선 설정">
      <i class="bi bi-gear limit-gear"></i>
    </button>
  </td>
  <td class="text-qty cell-current">${currentText}</td>
  <td class="text-center">
    <button type="button" class="icon-btn btn-detail" title="상세보기">
      <i class="bi bi-file-earmark-text"></i>
    </button>
  </td>
</tr>`;
        } else {
            return `
<tr
  data-storeitemno="${item.storeItemNo}"
  data-itemname="${item.itemName || ''}"
  data-category="${item.itemCategory || ''}"
  data-currentqty="${item.currentQuantity || 0}"
  data-unit="${item.stockUnit || 'ea'}"
  data-convertstock="${item.convertStock ?? ''}"
  data-supplyunit="${item.supplyUnit || 'box'}"
  data-limit="${limitVal != null ? limitVal : ''}"
  data-owner="${ownerAttr}"
  data-managerlimit="${managerLimit ?? ''}"
  data-storelimit="${storeLimit ?? ''}"
>
  <td class="text-code">${item.itemCode || ''}</td>
  <td>${item.itemName || ''}</td>
  <td>${item.itemCategory || ''}</td>
  <td class="text-qty cell-limit">
    ${limitText}
    <button type="button" class="icon-btn btn-open-limit" title="하한선 설정">
      <i class="bi bi-gear limit-gear"></i>
    </button>
  </td>
  <td class="text-qty cell-current">
    ${currentText}
    <button type="button" class="icon-btn btn-open-dispose" title="폐기 등록">
      <i class="bi bi-trash3"></i>
    </button>
  </td>
  <td class="text-center">
    <button type="button" class="icon-btn btn-detail" title="상세보기">
      <i class="bi bi-file-earmark-text"></i>
    </button>
  </td>
</tr>`;
        }
    }

    function renderTable(pageData) {
        const $tbody = $('#stockTableBody').empty();
        const colSpan = IS_MANAGER ? 7 : 6;
        if (!pageData || !pageData.content || pageData.content.length === 0) {
            $tbody.append(`<tr><td colspan="${colSpan}" class="text-center text-muted py-4">조회 결과가 없습니다.</td></tr>`);
            return;
        }
        pageData.content.forEach(item => { $tbody.append(buildRowHtml(item)); });
        markLowRows();
    }

    /*** 메인 페이저: 번호 5개 블록 & << < > >> ***/
    function renderPager(pageData) {
        const $pager = $('#mainPager').empty();
        if (!pageData || pageData.totalPages === 0) return;

        const total = pageData.totalPages;
        const current = (pageData.page || 0) + 1; // 0-base → 1-base

        const block = Math.floor((current - 1) / VISIBLE_PAGES);
        const start = block * VISIBLE_PAGES + 1;
        const end = Math.min(total, start + VISIBLE_PAGES - 1);
        const hasPrev = start > 1;
        const hasNext = end < total;

        function addItem(label, targetPage, disabled, active) {
            const liClass = 'page-item' + (disabled ? ' disabled' : '') + (active ? ' active' : '');
            $pager.append(`<li class="${liClass}"><a class="page-link" href="#" data-page="${targetPage}">${label}</a></li>`);
        }

        addItem('<<', 1, !hasPrev && current === 1, false);
        addItem('<', Math.max(1, start - 1), !hasPrev, false);
        for (let p = start; p <= end; p++) addItem(String(p), p, false, p === current);
        addItem('>', Math.min(total, end + 1), !hasNext, false);
        addItem('>>', total, !hasNext && current === total, false);
    }

    function loadPage(page) {
        if (IS_MANAGER) {
            if (!selectedStoreNo) { toast('먼저 직영점을 선택해 주세요.', 'warning'); return; }
        } else {
            if (!STORE_NO) { console.error('STORE_NO가 설정되지 않았습니다.'); return; }
        }

        const category = $('#categorySelect').val();
        const rawType = $('#searchTypeSelect').val();
        const searchType = normalizeSearchType(rawType);
        const keyword = ($('#searchKeyword').val() || '').trim();

        const params = new URLSearchParams();
        params.append('storeNo', IS_MANAGER ? selectedStoreNo : STORE_NO);
        params.append('size', PAGE_SIZE);
        if (category) params.append('category', category);
        if (keyword && searchType) { params.append('searchType', searchType); params.append('keyword', keyword); }

        const baseUrl = IS_MANAGER ? '/stock/storeItem/manager/list/' : '/stock/storeItem/store/list/';
        fetch(baseUrl + page + '?' + params.toString())
            .then(res => { if (!res.ok) throw new Error('목록 조회 실패'); return res.json(); })
            .then(data => {
                const total = data.totalPages || 1;
                if (page > total) {
                    window._currentPage = total;
                    const st = collectState(); st.page = total; pushUrl(st);
                    sessionStorage.setItem(STATE_KEY, JSON.stringify(st));
                    renderPager(data);
                    return loadPage(total);
                }
                renderTable(data);
                renderPager(data);
                window._currentPage = page;
                const st = collectState(); st.page = page; pushUrl(st);
                sessionStorage.setItem(STATE_KEY, JSON.stringify(st));
            })
            .catch(err => { console.error(err); toast('재고 목록 조회 중 오류가 발생했습니다.', 'danger'); });
    }

    /*** 조회/초기화/페이징 ***/
    $('#btnSearchExec').on('click', function () {
        if (IS_MANAGER && !selectedStoreNo) { toast('먼저 직영점을 선택해 주세요.', 'warning'); return; }
        loadPage(1);
    });
    $('#btnResetFilter').on('click', function () {
        $('#categorySelect').val('');
        $('#searchTypeSelect').val('ITEM_NAME');
        $('#searchKeyword').val('');
        if (IS_MANAGER) { if (selectedStoreNo) loadPage(1); }
        else { if (STORE_NO) loadPage(1); }
    });
    $(document).on('click', '#mainPager a.page-link', function (e) {
        e.preventDefault();
        const $li = $(this).parent();
        if ($li.hasClass('disabled') || $li.hasClass('active')) return;
        const page = parseInt($(this).data('page'), 10);
        if (!isNaN(page)) loadPage(page);
    });

    /*** 본사 전용: 직영점 검색 모달 열기 ***/
    if (IS_MANAGER && $('#btnOpenStoreSearch').length) {
        $('#btnOpenStoreSearch').on('click', function () {
            const initKw = $('#storeSearchKeyword').val() || '';
            if (window.StoreSearchModal?.open) {
                window.StoreSearchModal.open(initKw, function onSelect(storeNo, storeName) {
                    selectedStoreNo = storeNo;
                    selectedStoreName = storeName;
                    $('#storeSearchKeyword').val(storeName);
                    updateOverlay();

                    const st = collectState(); st.page = 1;
                    pushUrl(st);
                    sessionStorage.setItem(STATE_KEY, JSON.stringify(st));
                    loadPage(1);
                });
            } else {
                toast('직영점 검색 모듈이 로드되지 않았습니다. modalStoreSearch.js를 확인하세요.', 'warning');
            }
        });

        $('#storeSearchKeyword').on('input change', function () {
            const $overlay = $('#storeNoticeOverlay');
            if (!$overlay.length) return;
            const hasText = $.trim($(this).val()) !== '';
            if (hasText) $overlay.removeClass('d-flex').addClass('d-none');
            else $overlay.removeClass('d-none').addClass('d-flex');
        });
    }

    /*** 직영점 화면: 첫 진입 자동 1페이지 (복원 우선) ***/
    if (!IS_MANAGER && STORE_NO) {
        const restored = sessionStorage.getItem(STATE_KEY);
        if (restored) {
            try { const st = JSON.parse(restored); applyState(st); loadPage(st.page || 1); }
            catch { loadPage(1); }
        } else {
            const init = readUrl(); applyState(init); loadPage(init.page || 1);
        }
    }

    /*** 상세보기 (공통 더미) ***/
    $(document).on('click', '.btn-detail', function () {
        window.location.href = '/item/detail';
    });

    // 뒤로가기/복원
    window.addEventListener('pageshow', () => {
        const ss = sessionStorage.getItem(STATE_KEY);
        const st = history.state || readUrl() || (ss && JSON.parse(ss)) || null;
        applyState(st);
        if (IS_MANAGER ? selectedStoreNo : STORE_NO) { loadPage((st && st.page) || 1); }
        else { updateOverlay(); }
    });
    window.addEventListener('popstate', (e) => {
        const st = e.state || readUrl() || null;
        applyState(st);
        loadPage((st && st.page) || 1);
    });

    $('#storeSearchKeyword').on('input change', updateOverlay);

    // 내부 미세수정: 오타 방지용 no-op (문맥 유지)
    function theConstFix(){}

})(jQuery);
