(function () {
    'use strict';

    const $ = window.jQuery;
    const $body = $('body');

    // 화면 구분: 매니저(모달 버튼 존재) / 직영점
    const IS_MANAGER = $('#btnOpenStoreSearch').length > 0;

    // 상태
    let selectedStoreNo = null;
    let selectedStoreName = '';

    // 공통: 테이블 렌더
    function renderRows(list) {
        const $tb = $('#stockHistoryBody').empty();
        if (!list || list.length === 0) {
            $tb.append('<tr><td colspan="8" class="text-center text-muted py-4">조회 결과가 없습니다.</td></tr>');
            return;
        }
        list.forEach(r => {
            const dt = (r.changeDatetime || '').toString().replace('T', ' ').substring(0, 19);
            const qty = Number(r.changeQuantity || 0);
            const sign = qty > 0 ? '+' : '';
            const unit = r.stockUnit || '';
            const qtyHtml = `<td class="text-end ${qty>0?'chg-plus':'chg-minus'}">${sign}${qty.toLocaleString()} ${unit}</td>`;
            $tb.append(`
<tr>
  <td>${dt}</td>
  <td>${r.itemCategory || ''}</td>
  <td>${r.itemCode || ''}</td>
  <td>${r.itemName || ''}</td>
  <td>${r.changeReason || ''}</td>
  ${qtyHtml}
  <td class="text-end">${Number(r.currentQuantity||0).toLocaleString()} ${unit}</td>
  <td>${r.disposalReason || '-'}</td>
</tr>`);
        });
    }

    // 페이저
    function renderPager(resp, onGo) {
        const $p = $('#pager').empty();
        const total = resp?.totalPages || 1;
        const current = (resp?.page || 0) + 1;
        function li(label, target, disabled, active) {
            $p.append(`<li class="page-item${disabled?' disabled':''}${active?' active':''}">
        <a class="page-link" href="#" data-p="${target}">${label}</a></li>`);
        }
        li('«', 1, current===1, false);
        li('‹', Math.max(1, current-1), current===1, false);
        for (let p = resp.startPage; p <= resp.endPage; p++) li(String(p), p, false, p===current);
        li('›', Math.min(total, current+1), current===total, false);
        li('»', total, current===total, false);

        $p.off('click').on('click','a.page-link',function(e){
            e.preventDefault();
            const target = parseInt($(this).data('p'),10);
            if (!isNaN(target)) onGo(target);
        });
    }

    // 파라미터 수집(+ 서버 규격 맞춤)
    function collectParams() {
        const category = $('#categorySelect').val() || null;
        const reason = $('#reasonSelect').val() || null;
        const searchTypeRaw = $('#searchTypeSelect').val() || null; // ITEM_NAME / ITEM_CODE
        const keyword = ($('#searchKeyword').val() || '').trim() || null;
        const dateFrom = $('#dateFrom').val() || null;
        const dateTo = $('#dateTo').val() || null;

        const params = new URLSearchParams();
        if (IS_MANAGER) {
            if (!selectedStoreNo) return null; // 매니저는 storeNo 필수
            params.set('storeNo', selectedStoreNo);
        }
        if (category) params.set('category', category);
        if (reason) params.set('reason', reason);
        if (searchTypeRaw) params.set('searchType', searchTypeRaw);
        if (keyword) params.set('keyword', keyword);
        if (dateFrom) params.set('dateFrom', dateFrom);
        if (dateTo) params.set('dateTo', dateTo);
        params.set('size', '10');
        return params;
    }

    function toggleOverlay() {
        const $ov = $('#storeNoticeOverlay');
        if (!$ov.length) return;
        $ov.toggleClass('d-none', !!selectedStoreNo);
    }

    // 페이지 로드
    function loadPage(page) {
        const params = collectParams();
        if (IS_MANAGER && !params) {
            toggleOverlay();
            return;
        }
        const base = IS_MANAGER ? '/manager/stock/storeStock/list/' : '/store/stock/storeStock/list/';
        fetch(base + page + '?' + (params ? params.toString() : 'size=10'))
            .then(r => { if(!r.ok) throw new Error('조회 실패'); return r.json(); })
            .then(data => {
                renderRows(data.content || []);
                renderPager(data, loadPage);
                toggleOverlay();
            })
            .catch(console.error);
    }

    // 조회/초기화 버튼
    $('#btnSearch').on('click', () => loadPage(1));
    $('#btnReset').on('click', () => {
        $('#categorySelect').val('');
        $('#reasonSelect').val('');
        $('#searchTypeSelect').val('ITEM_NAME');
        $('#searchKeyword').val('');
        $('#dateFrom,#dateTo').val('');
        loadPage(1);
    });

    // 매니저: 직영점 모달
    if (IS_MANAGER) {
        $('#btnOpenStoreSearch').on('click', function(){
            if (!window.StoreSearchModal?.open) {
                alert('직영점 검색 모듈이 로드되지 않았습니다. modalStoreSearch.js를 확인하세요.');
                return;
            }
            const initKw = $('#storeSearchKeyword').val() || '';
            window.StoreSearchModal.open(initKw, function onSelect(storeNo, storeName){
                selectedStoreNo = storeNo;
                selectedStoreName = storeName || '';
                $('#storeSearchKeyword').val(selectedStoreName);
                loadPage(1);
            });
        });
        toggleOverlay();
    } else {
        // 직영점: 로그인 사용자의 storeNo로 서버가 강제 적용하므로 바로 조회
        loadPage(1);
    }
})();
