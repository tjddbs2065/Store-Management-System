(function ($) {
    'use strict';

    // ===== 설정 =====
    const API_BASE = '/api/items/list/';
    const PAGE_SIZE = 10;                // 한 페이지 10개 고정(필요시 서버와 함께 조정)

    // ===== 상태 =====
    let currentPage = 1;                 // UI는 1-base
    let totalPages = 1;

    // ===== 유틸 =====
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

        const VISIBLE = 5; // 번호 5개
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

        // 서버 파라미터 이름에 맞게 매핑
        const params = new URLSearchParams();
        params.set('size', String(PAGE_SIZE));
        if (category) params.set('itemCategory', category);

        if (keyword) {
            if (type === 'ITEM_NAME')      params.set('itemName', keyword);
            else if (type === 'ITEM_CODE') params.set('itemCode', keyword);
            else if (type === 'INGREDIENT')params.set('ingredientName', keyword);
            else if (type === 'SUPPLIER')  params.set('supplier', keyword);
        }
        return params.toString();
    }

    function loadPage(page) {
        const qs = collectQuery();
        fetch(API_BASE + page + (qs ? ('?' + qs) : ''))
            .then(res => { if (!res.ok) throw new Error('목록 조회 실패'); return res.json(); })
            .then(data => {
                // 응답 형식 호환 처리 (content / list, page/pageNo, totalPages)
                const list = data.content || data.list || [];
                totalPages = data.totalPages || 1;

                // 서버가 현재 페이지를 보내줄 수도 있음(1-base/0-base 혼재 대비)
                const responsePage = (data.pageNo || data.page || page);
                currentPage = Number(responsePage);
                if (data.page === 0 && !data.pageNo) currentPage = page; // 0-base 방지

                renderTable(list);
                renderPager(currentPage, totalPages);

                // 현재 페이지 범위 이상 요청 시 마지막 페이지로 재조회
                if (currentPage > totalPages && totalPages > 0) {
                    loadPage(totalPages);
                }
            })
            .catch(err => {
                console.error(err);
                toast('재고 품목 목록 조회 중 오류가 발생했습니다.', 'danger');
            });
    }

    // ===== 이벤트 =====
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

    // 초기 로드
    $(function () { loadPage(1); });

})(jQuery);
