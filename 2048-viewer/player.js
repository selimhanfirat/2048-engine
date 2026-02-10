function renderMetaData(meta) {
    const seedTitle = document.querySelector("#seed");
    seedTitle.textContent = meta.seed;
}

function renderStep(step) {
    const boardDiv = document.querySelector("#board");
    boardDiv.innerHTML = ""; // clear previous render

    const board = step.board;
    const gridSize = board.size;
    const cells = board.cells;

    for (let i = 0; i < gridSize; i++) {
        for (let j = 0; j < gridSize; j++) {
            const cell = cells[i][j];

            const gridElement = document.createElement("div");
            gridElement.classList.add("gridElement");
            gridElement.textContent = cell;

            boardDiv.appendChild(gridElement);
        }
    }
}


fetch("resources/example.json")
    .then(res => res.json())
    .then(
        data => {
            renderMetaData(data["meta"]);
            const lastStep = data.steps[data.steps.length - 1];
            renderStep(lastStep);
        }
    );
