function renderMetaData(meta) {
    const seedTitle = document.querySelector("#seed");
    seedTitle.textContent = meta.seed;
}

function renderStepOf(steps, index) {
    const boardDiv = document.querySelector("#board");
    boardDiv.innerHTML = ""; // clear old tiles

    const step = steps[index];
    const board = step.board;
    const gridSize = board.size;
    const cells = board.cells;

    boardDiv.style.gridTemplateColumns = `repeat(${gridSize}, 1fr)`;

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
    .then((res) => res.json())
    .then((data) => {
        renderMetaData(data.meta);

        const steps = data.steps;
        let i = 0;

        renderStepOf(steps, i);

        document.querySelector("#advanceBtn").addEventListener("click", () => {
            i = Math.min(i + 1, steps.length - 1); // don’t go past end
            renderStepOf(steps, i);
        });

        document.querySelector("#backBtn").addEventListener("click", () => {
            i = Math.max(i - 1, 0);
            renderStepOf(steps, i);
        });
    });
