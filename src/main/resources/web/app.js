"use strict";

const download = document.getElementById("download");
if (download) {
    download.addEventListener("click", () => {
        const blob = new Blob([document.getElementById("pattern").value], {type: "text/plain;charset=utf-8"});
        const url = URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = url;
        link.download = "tshirt-pattern.txt";
        link.click();
        setTimeout(() => URL.revokeObjectURL(url), 1000);
    });
}
