(function ($) {
    'use strict';

    const ROWS_PER_PAGE   = 5; // 행 5개
    const VISIBLE_PAGES   = 5; // 번호 5개

    let modalInstance     = null;
    let onSelectCallback  = null;

    // ★ 전체 목록 캐시
    let allStores = [];
    let storeList = [];

    let totalPages  = 1;
    let currentPage = 1;

    /*** 유틸 ***/
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
        return list.filter(v => re.test(v.storeName || '') || re.test(v.address || ''));
    }
    function getRegion(addr) {
        if (!addr) return '-';
        const parts = String(addr).trim().split(/\s+/);
        return parts.slice(0, 2).join(' ') || '-';
    }

    /*** 전체 목록 로딩(인라인 → API → DOM 파싱 최후) ***/
    async function fetchAllStores() {
        // 1) 인라인 JSON
        const inlineEl = document.getElementById('storeListData');
        if (inlineEl && inlineEl.textContent && inlineEl.textContent.trim()) {
            return safeJSON(inlineEl.textContent.trim()) || [];
        }

        // 2) API(키워드 없이 전체)
        try {
            const res = await fetch(getApiBase());
            if (res.ok) return await res.json(); // [{storeNo, storeName, address}, ...]
        } catch (e) {
            console.error('[modalStoreSearch] API error:', e);
        }

        // 3) 이미 렌더된 TR 파싱(최후의 수단)
        const $pre = $('#storeModalTableBody tr');
        if ($pre.length) {
            return $pre.map(function () {
                const $tds = $(this).children('td');
                const $btn = $(this).find('.btn-select-store');
                return {
                    storeNo:   Number($btn.data('storeno')) || null,
                    storeName: String($tds.eq(0).text()).trim(),
                    address:   String($tds.eq(1).text()).trim()
                };
            }).get().filter(v => v.storeName);
        }

        return [];
    }

    /*** 페이저 렌더링(번호 5개 블록) ***/
    function blockRange() {
        const blockIdx = Math.floor((currentPage - 1) / VISIBLE_PAGES);
        const start    = blockIdx * VISIBLE_PAGES + 1;
        const end      = Math.min(totalPages, start + VISIBLE_PAGES - 1);
        return { start, end };
    }
    function rebuildPager() {
        const $p = $('#storeModalPager').empty();
        totalPages = Math.max(1, Math.ceil(storeList.length / ROWS_PER_PAGE));

        const { start, end } = blockRange();

        function li(targetPage, label) {
            return $('<li class="page-item"><a class="page-link" href="#"></a></li>')
                .find('a').attr('data-page', String(targetPage)).html(label).end();
        }

        $p.append(li(1,  '<<'));
        $p.append(li(Math.max(1, start - 1), '<'));
        for (let i = start; i <= end; i++) $p.append(li(i, String(i)));
        $p.append(li(Math.min(totalPages, end + 1),  '>'));
        $p.append(li(totalPages, '>>'));

        paintPager();
    }
    function paintPager() {
        const $p = $('#storeModalPager');

        // 현재 블록이 아니면 재빌드
        const nums = $p.find('a.page-link').map(function () {
            const v = $(this).data('page');
            return /^\d+$/.test(String(v)) ? parseInt(v,10) : null;
        }).get().filter(v => v != null);
        const curMin = nums.length ? Math.min(...nums) : 1;
        const curMax = nums.length ? Math.max(...nums) : 1;
        if (currentPage < curMin || currentPage > curMax) {
            rebuildPager();
            return;
        }

        $p.find('.page-item').removeClass('active disabled');
        $p.find(`a.page-link[data-page="${currentPage}"]`).parent().addClass('active');

        // 양 끝 비활성
        if (currentPage === 1) {
            $p.find('a.page-link').filter(function(){ return $(this).text()==='<<' || $(this).text()==='<' ; }).parent().addClass('disabled');
        }
        if (currentPage === totalPages) {
            $p.find('a.page-link').filter(function(){ return $(this).text()==='>>' || $(this).text()==='>' ; }).parent().addClass('disabled');
        }
    }

    /*** 목록 렌더링 ***/
    function renderRows() {
        const $tb = $('#storeModalTableBody').empty();
        if (!storeList || storeList.length === 0) {
            $tb.append('<tr><td colspan="3" class="text-center text-muted py-3">직영점이 없습니다.</td></tr>');
            return;
        }
        const s = (currentPage - 1) * ROWS_PER_PAGE;
        const e = s + ROWS_PER_PAGE;
        storeList.slice(s, e).forEach(sv => {
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
</tr>`);
        });
    }
    function setAndRender(list) {
        storeList   = list || [];
        currentPage = 1;
        rebuildPager();
        renderRows();
    }

    /*** 이벤트 ***/
    $(document).on('click', '#storeModalPager a.page-link', function (e) {
        e.preventDefault();
        const $li = $(this).parent();
        if ($li.hasClass('disabled') || $li.hasClass('active')) return;

        const target = parseInt($(this).data('page'), 10);
        if (!isNaN(target)) {
            currentPage = target;
            paintPager();
            renderRows();
        }
    });

    $(document).on('click', '#btnSearchStoreExec', async function () {
        const kw = $('#storeSearchInput').val() || '';
        if (allStores.length === 0) allStores = await fetchAllStores();
        setAndRender(filterByKeyword(allStores, kw));
    });

    $(document).on('click', '#btnSearchStoreReset', async function () {
        $('#storeSearchInput').val('');
        if (allStores.length === 0) allStores = await fetchAllStores();
        setAndRender(allStores.slice());
    });

    $(document).on('keydown', '#storeSearchInput', async function (e) {
        if (e.key !== 'Enter') return;
        const kw = $(this).val() || '';
        if (allStores.length === 0) allStores = await fetchAllStores();
        setAndRender(filterByKeyword(allStores, kw));
    });

    $(document).on('click', '#storeModalTableBody .btn-select-store', function () {
        const storeNo   = $(this).data('storeno');
        const storeName = $(this).data('store');
        if (typeof onSelectCallback === 'function') onSelectCallback(storeNo, storeName);
        if (modalInstance) modalInstance.hide();
    });

    /*** 퍼블릭 API ***/
    window.StoreSearchModal = {
        /**
         * @param {string} initialKeyword
         * @param {(storeNo:number, storeName:string)=>void} onSelect
         */
        /* /resources/static/js/modalStoreSearch.js */
            open: async function (initialKeyword, onSelect) {
        onSelectCallback = onSelect || null;

        if (!modalInstance) {
            const el = document.getElementById('storeSearchModal');
            if (!el) {
                // alert → toast(있으면) 폴백
                if (window.toast) toast('storeSearchModal 요소가 없습니다.', 'warning');
                else alert('storeSearchModal이 페이지에 없습니다.');
                return;
            }
            modalInstance = new bootstrap.Modal(el);
        }

        $('#btnSearchStoreExec').removeClass('btn-primary').addClass('btn-main');

        $('#storeSearchInput').val(initialKeyword || '');
        allStores = await fetchAllStores();
        setAndRender(filterByKeyword(allStores, initialKeyword || ''));
        modalInstance.show();
    }

};

})(jQuery);
