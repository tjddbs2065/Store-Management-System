(function ($) {
    'use strict';

    // ===== 공통 상수 =====
    const API_LIST_BASE  = '/api/items/list/';
    const API_ITEM_BASE  = '/api/items';
    const PAGE_SIZE      = 10;

    // ===== 상태 (목록용) =====
    let currentPage = 1;

    // ===== 공통 Toast =====
    function toast(message, type) {
        let box = document.getElementById('toastBox');
        if (!box) {
            box = document.createElement('div');
            box.id = 'toastBox';
            box.style.cssText = 'position:fixed;right:16px;bottom:16px;z-index:9999';
            document.body.appendChild(box);
        }
        const el = document.createElement('div');
        el.className = 'alert alert-' + (type || 'success') + ' shadow-sm py-2 px-3 mb-2';
        el.textContent = message;
        box.appendChild(el);
        setTimeout(() => {
            el.style.transition = 'opacity .2s';
            el.style.opacity = '0';
            setTimeout(() => el.remove(), 200);
        }, 1800);
    }

    // =========================
    // 1) 목록 화면 로직
    // =========================
    function buildRow(item) {
        const priceText = (item.itemPrice != null) ? Number(item.itemPrice).toLocaleString() : '-';
        const itemNo = item.itemNo;
        return `
<tr>
  <td>${item.itemCategory ?? ''}</td>
  <td class="text-code">${item.itemCode ?? ''}</td>
  <td>${item.itemName ?? ''}</td>
  <td>${item.ingredientName ?? ''}</td>
  <td>${item.supplier ?? ''}</td>
  <td class="text-price">${priceText}</td>
  <td class="text-center">
    <button class="icon-btn detail-btn" data-itemno="${itemNo ?? ''}" title="상세보기">
      <i class="bi bi-file-earmark-text"></i>
    </button>
  </td>
</tr>`;
    }

    function renderTable(list) {
        const $tb = $('#itemTableBody').empty();
        if (!list || list.length === 0) {
            $tb.append('<tr><td colspan="7" class="text-center text-muted py-4">조회 결과가 없습니다.</td></tr>');
            return;
        }
        list.forEach(it => $tb.append(buildRow(it)));
    }

    function renderPager(current, total) {
        const $p = $('#pager').empty();
        if (total < 1) return;

        const VISIBLE = 5;
        const blockIndex = Math.floor((current - 1) / VISIBLE);
        const start = blockIndex * VISIBLE + 1;
        const end = Math.min(start + VISIBLE - 1, total);

        function add(label, target, disabled, active) {
            const liClass = 'page-item' + (disabled ? ' disabled' : '') + (active ? ' active' : '');
            $p.append(`<li class="${liClass}"><a class="page-link" href="#" data-page="${target}">${label}</a></li>`);
        }

        add('«', 1, current === 1, false);
        add('‹', Math.max(1, current - 1), current === 1, false);
        for (let p = start; p <= end; p++) add(String(p), p, false, p === current);
        add('›', Math.min(total, current + 1), current === total, false);
        add('»', total, current === total, false);
    }

    function collectQuery() {
        const category = $('#categorySelect').val() || '';
        const type = $('#searchTypeSelect').val();
        const keyword = ($('#searchKeyword').val() || '').trim();

        const params = new URLSearchParams();
        params.set('size', String(PAGE_SIZE));
        if (category) params.set('itemCategory', category);

        if (keyword) {
            if (type === 'ITEM_NAME')       params.set('itemName', keyword);
            else if (type === 'ITEM_CODE')  params.set('itemCode', keyword);
            else if (type === 'INGREDIENT') params.set('ingredientName', keyword);
            else if (type === 'SUPPLIER')   params.set('supplier', keyword);
        }
        return params.toString();
    }

    function loadPage(page) {
        const qs = collectQuery();
        fetch(API_LIST_BASE + page + (qs ? ('?' + qs) : ''))
            .then(res => { if (!res.ok) throw new Error('목록 조회 실패'); return res.json(); })
            .then(data => {
                const list = data.content || data.list || [];
                const totalPages = data.totalPages || 1;

                let responsePage = data.pageNo || data.page || page;
                if (typeof responsePage === 'string') responsePage = parseInt(responsePage, 10);
                currentPage = (responsePage ?? page) + (data.page !== undefined ? 1 : 0);
                if (!data.pageNo && data.page === 0) currentPage = page;

                renderTable(list);
                renderPager(currentPage, totalPages);

                if (currentPage > totalPages && totalPages > 0) {
                    loadPage(totalPages);
                }
            })
            .catch(err => {
                console.error(err);
                toast('재고 품목 목록 조회 중 오류가 발생했습니다.', 'danger');
            });
    }

    function initListPage() {
        if (!$('#itemTableBody').length) return;

        // 페이저 클릭
        $(document).on('click', '#pager a.page-link', function (e) {
            e.preventDefault();
            const target = parseInt($(this).data('page'), 10);
            if (!isNaN(target) && target !== currentPage) loadPage(target);
        });

        $('#btnSearch').on('click', function () {
            loadPage(1);
        });

        $('#btnReset').on('click', function () {
            $('#categorySelect').val('');
            $('#searchTypeSelect').val('ITEM_NAME');
            $('#searchKeyword').val('');
            loadPage(1);
        });

        // 상세 이동
        $(document).on('click', '.detail-btn', function () {
            const itemNo = $(this).data('itemno');
            if (itemNo) window.location.href = '/item/detail?itemNo=' + itemNo;
            else window.location.href = '/item/detail';
        });

        loadPage(1);
    }

    // =========================
    // 2) 상세 화면 로직
    // =========================
    function fillDetail(dto) {
        $('#d-category').text(dto.itemCategory || '-');
        $('#d-itemCode').text(dto.itemCode || '-');
        $('#d-itemName').text(dto.itemName || '-');
        $('#d-ingredientName').text(dto.ingredientName || '-');

        $('#d-stockUnit').text(dto.stockUnit || '-');
        $('#d-supplyUnit').text(dto.supplyUnit || '-');

        if (dto.convertStock != null) {
            $('#d-convert').text(dto.convertStock.toLocaleString() + ' ' + (dto.stockUnit || ''));
        } else {
            $('#d-convert').text('-');
        }

        if (dto.itemPrice != null) {
            $('#d-itemPrice').text(dto.itemPrice.toLocaleString() + ' 원');
        } else {
            $('#d-itemPrice').text('-');
        }

        $('#d-supplier').text(dto.supplier || '-');
        $('#d-storageType').text(dto.storageType || '-');
        $('#d-expType').text(dto.expirationType || '-');

        if (dto.expiration != null && dto.expirationType === '입고 후 n일') {
            $('#d-expiration').text(dto.expiration + ' 일');
        } else if (dto.expiration != null) {
            $('#d-expiration').text(String(dto.expiration));
        } else {
            $('#d-expiration').text('-');
        }

        $('#d-note').text(dto.note || '-');

        if (dto.itemImage) {
            $('#d-itemImage').attr('src', dto.itemImage);
        }
    }

    function initDetailPage() {
        const $body = $('body');
        const itemNo = $body.data('itemNo');
        if (!itemNo || !$('#d-itemCode').length) return;

        // 데이터 로딩
        fetch(API_ITEM_BASE + '/' + itemNo)
            .then(res => { if (!res.ok) throw new Error('상세 조회 실패'); return res.json(); })
            .then(dto => fillDetail(dto))
            .catch(err => {
                console.error(err);
                toast('품목 상세 조회 중 오류가 발생했습니다.', 'danger');
            });

        // 버튼 동작
        $('#btnEdit').on('click', function () {
            window.location.href = '/item/set?itemNo=' + itemNo;
        });

        $('#btnDelete').on('click', function () {
            if (!confirm('이 품목을 삭제하시겠습니까?')) return;

            fetch(API_ITEM_BASE + '/' + itemNo, { method: 'DELETE' })
                .then(res => {
                    if (res.status === 403) throw new Error('권한이 없습니다.');
                    if (!res.ok) throw new Error('삭제 실패');
                    toast('삭제가 완료되었습니다.', 'success');
                    setTimeout(() => { window.location.href = '/item/get'; }, 800);
                })
                .catch(err => {
                    console.error(err);
                    toast(err.message || '삭제 중 오류가 발생했습니다.', 'danger');
                });
        });
    }

    // =========================
    // 3) 등록/수정 공통 - 폼
    // =========================
    function bindExpirationBox() {
        const $type = $('#expirationType');
        if (!$type.length) return;

        function toggleBox() {
            const v = $type.val();
            if (v === '입고 후 n일') {
                $('#expirationDaysBox').show();
            } else {
                $('#expirationDaysBox').hide();
                $('#expiration').val('');
            }
        }

        $type.on('change', toggleBox);
        toggleBox();
    }

    function bindImageDrop(dropId) {
        const $drop = $(dropId);
        const $file = $('#itemImage');
        if (!$drop.length || !$file.length) return;

        $drop.on('click', function () {
            $file.trigger('click');
        });

        $drop.on('dragover', function (e) {
            e.preventDefault();
            $(this).css('border-color', '#999');
        }).on('dragleave', function () {
            $(this).css('border-color', '#ccc');
        }).on('drop', function (e) {
            e.preventDefault();
            const dt = e.originalEvent.dataTransfer;
            if (dt && dt.files) {
                $file[0].files = dt.files;
            }
            $(this).css('border-color', '#ccc');
        });
    }

    function collectFormData() {
        const dto = {};

        dto.itemCategory   = ($('#itemCategory').val() || '').trim();
        dto.itemCode       = ($('#itemCode').val() || '').trim();
        dto.itemName       = ($('#itemName').val() || '').trim();
        dto.ingredientName = ($('#ingredientName').val() || '').trim();

        dto.stockUnit    = ($('#stockUnit').val() || '').trim();
        dto.supplyUnit   = ($('#supplyUnit').val() || '').trim();
        dto.convertStock = $('#convertStock').val() ? parseInt($('#convertStock').val(), 10) : null;
        dto.itemPrice    = $('#itemPrice').val() ? parseInt($('#itemPrice').val(), 10) : null;
        dto.supplier     = ($('#supplier').val() || '').trim();

        dto.storageType    = $('input[name="storageType"]:checked').val() || null;
        dto.expirationType = $('#expirationType').val() || null;
        dto.expiration     = $('#expiration').val() ? parseInt($('#expiration').val(), 10) : null;

        dto.note        = ($('#note').val() || '').trim();
        // 이미지 업로드는 지금은 경로 문자열만 사용 (필요시 추가 구현)
        // dto.itemImage = ...;

        return dto;
    }

    function validateForm(dto) {
        if (!dto.itemCategory) { toast('카테고리를 선택해 주세요.', 'warning'); return false; }
        if (!dto.itemCode)     { toast('품목 코드를 입력해 주세요.', 'warning'); return false; }
        if (!dto.itemName)     { toast('품목 명을 입력해 주세요.', 'warning'); return false; }
        if (!dto.ingredientName){ toast('재료 명을 입력해 주세요.', 'warning'); return false; }
        if (!dto.stockUnit)    { toast('기준 단위를 선택해 주세요.', 'warning'); return false; }
        if (!dto.supplyUnit)   { toast('공급 단위를 선택해 주세요.', 'warning'); return false; }
        if (dto.itemPrice == null || isNaN(dto.itemPrice) || dto.itemPrice <= 0) {
            toast('공급 가격을 1 이상으로 입력해 주세요.', 'warning'); return false;
        }
        if (!dto.supplier)     { toast('공급사를 입력해 주세요.', 'warning'); return false; }

        if (dto.expirationType === '입고 후 n일' && (dto.expiration == null || dto.expiration <= 0)) {
            toast('보관기한 일수를 입력해 주세요.', 'warning'); return false;
        }
        return true;
    }

    function initAddPage() {
        if (!$('#itemFormAdd').length) return;

        bindExpirationBox();
        bindImageDrop('#imgDropAdd');

        $('#btnItemSave').on('click', function () {
            const dto = collectFormData();
            if (!validateForm(dto)) return;

            fetch(API_ITEM_BASE, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json;charset=UTF-8' },
                body: JSON.stringify(dto)
            })
                .then(res => {
                    if (res.status === 403) throw new Error('권한이 없습니다.');
                    if (!res.ok) return res.text().then(t => { throw new Error(t || '등록 실패'); });
                    return res.json();
                })
                .then(() => {
                    toast('등록이 완료되었습니다.', 'success');
                    setTimeout(() => { window.location.href = '/item/get'; }, 800);
                })
                .catch(err => {
                    console.error(err);
                    toast(err.message || '등록 중 오류가 발생했습니다.', 'danger');
                });
        });
    }

    function fillForm(dto) {
        $('#itemCategory').val(dto.itemCategory || '');
        $('#itemCode').val(dto.itemCode || '');
        $('#itemName').val(dto.itemName || '');
        $('#ingredientName').val(dto.ingredientName || '');

        $('#stockUnit').val(dto.stockUnit || '');
        $('#supplyUnit').val(dto.supplyUnit || '');
        if (dto.convertStock != null) $('#convertStock').val(dto.convertStock);
        if (dto.itemPrice != null) $('#itemPrice').val(dto.itemPrice);
        $('#supplier').val(dto.supplier || '');

        if (dto.storageType) {
            $('input[name="storageType"][value="' + dto.storageType + '"]').prop('checked', true);
        }

        $('#expirationType').val(dto.expirationType || '');
        if (dto.expiration != null) $('#expiration').val(dto.expiration);

        $('#note').val(dto.note || '');
    }

    function initEditPage() {
        if (!$('#itemFormEdit').length) return;

        const itemNo = $('body').data('itemNo');
        if (!itemNo) return;

        bindExpirationBox();
        bindImageDrop('#imgDropEdit');

        // 기존 데이터 불러오기
        fetch(API_ITEM_BASE + '/' + itemNo)
            .then(res => { if (!res.ok) throw new Error('상세 조회 실패'); return res.json(); })
            .then(dto => {
                fillForm(dto);
                // 보관기한 박스 표시 상태 맞추기
                $('#expirationType').trigger('change');
            })
            .catch(err => {
                console.error(err);
                toast('품목 정보 조회 중 오류가 발생했습니다.', 'danger');
            });

        $('#btnItemUpdate').on('click', function () {
            const dto = collectFormData();
            if (!validateForm(dto)) return;

            fetch(API_ITEM_BASE + '/' + itemNo, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json;charset=UTF-8' },
                body: JSON.stringify(dto)
            })
                .then(res => {
                    if (res.status === 403) throw new Error('권한이 없습니다.');
                    if (!res.ok) return res.text().then(t => { throw new Error(t || '수정 실패'); });
                    return res.json();
                })
                .then(() => {
                    toast('수정이 완료되었습니다.', 'success');
                    setTimeout(() => { window.location.href = '/item/detail?itemNo=' + itemNo; }, 800);
                })
                .catch(err => {
                    console.error(err);
                    toast(err.message || '수정 중 오류가 발생했습니다.', 'danger');
                });
        });
    }

    // =========================
    // 초기 진입 시 분기
    // =========================
    $(function () {
        initListPage();
        initDetailPage();
        initAddPage();
        initEditPage();
    });

})(jQuery);
