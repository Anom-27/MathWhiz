/* Math Whiz: one "Run Seed Generator" button per question.
   The button posts that question's inputs to /game (action=seed) and the server answers with JSON:
   {ok, calc, question, options:[{value,label}]}  or  {ok:false, error}.
   Everything from the server is shown with textContent, never as HTML. */
(function () {
    'use strict';

    var form = document.getElementById('gameForm');
    var submitBtn = document.getElementById('submitBtn');
    var submitHint = document.getElementById('submitHint');
    var cards = form.querySelectorAll('.q[data-q]');
    var ready = {};

    function updateSubmit() {
        var left = 0;
        cards.forEach(function (c) { if (!ready[c.dataset.q]) { left++; } });
        submitBtn.disabled = left > 0;
        submitHint.hidden = left === 0;
        submitHint.textContent = 'Run the seed generator for ' + left + ' more question' + (left === 1 ? '' : 's') + ' to unlock Submit.';
    }

    function el(tag, className, text) {
        var e = document.createElement(tag);
        if (className) { e.className = className; }
        if (text !== undefined) { e.textContent = text; }
        return e;
    }

    cards.forEach(function (card) {
        var q = card.dataset.q;
        var btn = card.querySelector('.seed-btn');
        var out = card.querySelector('.seed-out');
        var err = card.querySelector('.card-error');
        var fields = card.querySelectorAll('.inputs input, .inputs select');

        function showError(message) {
            err.textContent = message;
            err.hidden = false;
        }

        function reset() {                       // the player changed an input: the old result no longer matches
            ready[q] = false;
            out.textContent = '';
            err.hidden = true;
            updateSubmit();
        }

        function render(data) {
            out.textContent = '';
            out.appendChild(el('p', 'calc', data.calc));
            out.appendChild(el('p', 'ask', data.question));
            data.options.forEach(function (o) {
                var label = el('label', 'choice');
                var radio = document.createElement('input');
                radio.type = 'radio';
                radio.name = q + 'Answer';
                radio.value = o.value;
                radio.required = true;
                label.appendChild(radio);
                label.appendChild(el('span', '', o.label));
                out.appendChild(label);
            });
            ready[q] = true;
            updateSubmit();
        }

        fields.forEach(function (f) {
            f.addEventListener('input', reset);
            f.addEventListener('change', reset);
        });

        btn.addEventListener('click', function () {
            for (var i = 0; i < fields.length; i++) {          // use the browser's own validation messages
                if (!fields[i].checkValidity()) { fields[i].reportValidity(); return; }
            }
            var body = new URLSearchParams();
            body.append('action', 'seed');
            body.append('q', q);
            fields.forEach(function (f) { body.append(f.name, f.value); });

            btn.disabled = true;
            err.hidden = true;
            fetch(form.getAttribute('action'), {
                method: 'POST',
                credentials: 'same-origin',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                body: body
            }).then(function (r) { return r.json(); })
              .then(function (data) {
                  btn.disabled = false;
                  if (data.ok) { render(data); } else { out.textContent = ''; ready[q] = false; updateSubmit(); showError(data.error); }
              })
              .catch(function () {
                  btn.disabled = false;
                  showError('Something went wrong. Please reload the page and try again.');
              });
        });
    });

    updateSubmit();
})();
