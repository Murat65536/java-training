// mdBook's copy button leaves out hidden lines. The "Putting it together" listings hide the parts
// that didn't change, but students need the whole program when they copy one, so always copy
// everything. (mdBook's copy button looks this function up by name every time it's clicked.)
window.playground_text = function (playground) {
    return playground.querySelector('code').textContent;
};
