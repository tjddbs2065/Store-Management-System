function fetchUtil(url, action, method="GET"){
    fetch(url, {method: method})
    .then(data => data.json())
    .then(action)
    .catch(err => console.error(err));
}

$(document).on("click", "#pagination .page-link", function () {
    const page = $(this).data("page");  // data-page 값 가져오기

    // 숫자일 경우
    if (!isNaN(page)) {
        loadPage(page);
    }

    // 이전/다음
    if (page === "prev") {
        const current = $("#pagination .active a").data("page");
        if (current > 1) loadPage(current - 1);
    }

    if (page === "next") {
        const current = $("#pagination .active a").data("page");
        loadPage(current + 1);
    }
});

function updatePaginationUI(page, totalPages) {
    const pag = $("#pagination");
    pag.find(".page-item").removeClass("active");

    // 현재 페이지 활성화
    pag.find(`a[data-page='${page}']`).closest(".page-item").addClass("active");

    // 이전 버튼 비활성화
    if (page == 1) {
        pag.find("a[data-page='prev']").closest(".page-item").addClass("disabled");
    } else {
        pag.find("a[data-page='prev']").closest(".page-item").removeClass("disabled");
    }

    // 다음 버튼 비활성화
    if (page == totalPages) {
        pag.find("a[data-page='next']").closest(".page-item").addClass("disabled");
    } else {
        pag.find("a[data-page='next']").closest(".page-item").removeClass("disabled");
    }

    renderPagination(page, totalPages);
}

function renderPagination(page, totalPages) {
    let html = `
        <li class="page-item ${page == 1 ? "disabled" : ""}">
            <a class="page-link" data-page="prev">이전</a>
        </li>
    `;

    for (let i = 1; i <= totalPages; i++) {
        html += `
            <li class="page-item ${i == page ? "active" : ""}">
                <a class="page-link" data-page="${i}">${i}</a>
            </li>
        `;
    }

    html += `
        <li class="page-item ${page == totalPages ? "disabled" : ""}">
            <a class="page-link" data-page="next">다음</a>
        </li>
    `;

    $("#pagination").html(html);
}