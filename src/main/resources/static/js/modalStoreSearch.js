(function ($) {
    'use strict';

    const ROWS_PER_PAGE = 5;

    let modalInstance = null;
    let onSelectCallback = null;

    let allStores = [];
    let storeList = [];
    let currentPage = 1;

    function getApiBase() {
        const el = document.getElementById('storeSearchModal');
        return el?.dataset?.api || '/storeSearch/modal';
    }

    function safeJSON(text) {
        try { return JSON.parse(text); } catch { return null; }
    }

    function escapeReg(s) { return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'); }

    function filterByKeyword(list, keyword) {
        const kw = (keyword || '').trim();
        if (!kw) return list.slice();
        const re = new RegExp(escapeReg(kw), 'i');
        return list.filter(v =>
            re.test(v.storeName || '') ||
            re.test(v.address || '')
        );
    }

    function getRegion(addr) {
        if (!addr) return '-';
        const parts = String(addr).trim().split(/\s+/);
        return parts.slice(0, 2).join(' ') || '-';
    }

    async function fetchAllStores() {
        const inlineEl = document.getElementById('storeListData');
        if (inlineEl && inlineEl.textContent && inlineEl.textContent.trim()) {
            return safeJSON(inlineEl.textContent.trim()) || [];
        }

        try {
            const res = await fetch(getApiBase());
            if (res.ok) return await res.json();
        } catch (e) {
            console.error('[modalStoreSearch] API error:', e);
        }

        const $pre = $('#storeModalTableBody tr');
        if ($pre.length) {
            return $pre.map(function () {
                const $tds = $(this).children('td');
                const $btn = $(this).find('.btn-select-store');
                return {
                    storeNo: Number($btn.data('storeno')) || null,
                    storeName: String($tds.eq(0).text()).trim(),
                    address: String($tds.eq(1).text()).trim()
                };
            }).get().filter(v => v.storeName);
        }

        return [];
    }

    /*** 본문 util.js 페이징 재사용하기 위한 함수 ***/
    function applyPaging() {
        const totalPages = Math.max(1, Math.ceil(storeList.length / ROWS_PER_PAGE));

        updatePaginationUI_Modal(currentPage, totalPages);
    }

    /*** util.js 기반 "모달 전용 페이징" ***/
    function updatePaginationUI_Modal(current, totalPages) {
        const pag = $("#storeModalPagination");
        pag.empty();

        const blockSize = 5;
        const currentBlock = Math.floor((current - 1) / blockSize);
        const startPage = currentBlock * blockSize + 1;
        const endPage = Math.min(startPage + blockSize - 1, totalPages);

        pag.append(`
            <li class="page-item ${current === 1 ? "disabled" : ""}">
                <a class="page-link modal-page" data-page="prev">이전</a>
            </li>
        `);

        for (let i = startPage; i <= endPage; i++) {
            pag.append(`
                <li class="page-item ${i === current ? "active" : ""}">
                    <a class="page-link modal-page" data-page="${i}">${i}</a>
                </li>
            `);
        }

        pag.append(`
            <li class="page-item ${current === totalPages ? "disabled" : ""}">
                <a class="page-link modal-page" data-page="next">다음</a>
            </li>
        `);
    }

    /*** 모달 내부 테이블 렌더 ***/
    function renderRows() {
        const $tb = $('#storeModalTableBody').empty();

        if (!storeList || storeList.length === 0) {
            $tb.append('<tr><td colspan="3" class="text-center py-3">직영점이 없습니다.</td></tr>');
            return;
        }

        const start = (currentPage - 1) * ROWS_PER_PAGE;
        const slice = storeList.slice(start, start + ROWS_PER_PAGE);

        slice.forEach(sv => {
            $tb.append(`
<tr>
  <td>${sv.storeName || '-'}</td>
  <td>${getRegion(sv.address)}</td>
  <td class="text-center">
    <button type="button" class="btn btn-sm btn-main btn-select-store"
            data-storeno="${sv.storeNo}" data-store="${sv.storeName}">
      선택
    </button>
  </td>
</tr>
`);
        });
    }

    /*** 검색 후 전체 다시 렌더 ***/
    function setAndRender(list) {
        storeList = list || [];
        currentPage = 1;
        applyPaging();
        renderRows();
    }

    /*** 페이징 이벤트 util.js 구조 그대로 적용 ***/
    $(document).on("click", ".modal-page", function () {
        const page = $(this).data("page");
        const totalPages = Math.max(1, Math.ceil(storeList.length / ROWS_PER_PAGE));

        if (page === "prev") {
            if (currentPage > 1) currentPage--;
        } else if (page === "next") {
            if (currentPage < totalPages) currentPage++;
        } else if (!isNaN(page)) {
            currentPage = page;
        }

        applyPaging();
        renderRows();
    });

    /*** 검색 버튼 ***/
    $(document).on('click', '#btnSearchStoreExec', async function () {
        const kw = $('#storeSearchInput').val() || '';
        if (allStores.length === 0) allStores = await fetchAllStores();
        setAndRender(filterByKeyword(allStores, kw));
    });

    /*** 초기화 버튼 ***/
    $(document).on('click', '#btnSearchStoreReset', async function () {
        $('#storeSearchInput').val('');
        if (allStores.length === 0) allStores = await fetchAllStores();
        setAndRender(allStores.slice());
    });

    /*** Enter 검색 ***/
    $(document).on('keydown', '#storeSearchInput', async function (e) {
        if (e.key !== 'Enter') return;
        const kw = $(this).val() || '';
        if (allStores.length === 0) allStores = await fetchAllStores();
        setAndRender(filterByKeyword(allStores, kw));
    });

    /*** 지점 선택 ***/
    $(document).on('click', '#storeModalTableBody .btn-select-store', function () {
        const storeNo = $(this).data('storeno');
        const storeName = $(this).data('store');
        if (typeof onSelectCallback === 'function') onSelectCallback(storeNo, storeName);
        if (modalInstance) modalInstance.hide();
    });

    /*** 외부 API ***/
    window.StoreSearchModal = {
        open: async function (initialKeyword, onSelect) {
            onSelectCallback = onSelect || null;

            if (!modalInstance) {
                const el = document.getElementById('storeSearchModal');
                modalInstance = new bootstrap.Modal(el);
            }

            $('#storeSearchInput').val(initialKeyword || '');

            allStores = await fetchAllStores();
            setAndRender(filterByKeyword(allStores, initialKeyword || ''));

            modalInstance.show();
        }
    };

})(jQuery);
