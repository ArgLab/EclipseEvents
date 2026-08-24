const DATA = JSON.parse(document.getElementById('report-data').textContent);

// ---------- helpers ----------
const $ = (h) => {
  const t = document.createElement('template');
  t.innerHTML = h.trim();
  return t.content.firstChild;
};

const esc = (s) =>
  String(s == null ? '' : s).replace(/[&<>"]/g, (c) => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
  }[c]));

const pct = (cov, miss) => {
  const t = cov + miss;
  return t ? Math.round((100 * cov) / t) : "N/A";
};

const covColor = (p) => (p == null ? '#8a929b' : p >= 80 ? '#2f8a2a' : '#c5281d');

function fmtDate(s) {
  if (!s) return '';
  const d = new Date(s);
  if (isNaN(d)) return s;
  return d.toLocaleString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

// ---------- compute ----------
const stu = DATA.studentUnitTests || [];
const ts = DATA.tsUnitTests || [];

const cnt = (arr) => ({
  total: arr.length,
  fail: arr.filter((t) => t.fail).length,
  pass: arr.filter((t) => !t.fail).length,
});

const sStu = cnt(stu);
const sTs = cnt(ts);

const cov = DATA.coverageData || [];
const covTot = {};
['instruction', 'branch', 'line', 'complexity', 'method'].forEach((m) => {
  const c = cov.reduce((a, x) => a + (x[m + 'Covered'] || 0), 0);
  const mi = cov.reduce((a, x) => a + (x[m + 'Missed'] || 0), 0);
  covTot[m] = { cov: c, miss: mi, pct: pct(c, mi) };
});

const chk = DATA.checkstyleNotifications || [];
const pmd = DATA.pmdNotifications || [];
const spot = DATA.spotBugsNotifications || [];
const warnTotal = chk.length + pmd.length + spot.length;

const counts = DATA.countsData || [];
const diffs = DATA.fileDiffs || [];
const cd = DATA.commitDetails || {};

// ---------- render ----------
const root = document.getElementById('root');

function section(title, icon, countHtml, bodyNode, collapsed) {
  const sec = $(`<div class="section ${collapsed ? 'collapsed' : ''}">
    <div class="sec-head">
      <svg class="twistie" viewBox="0 0 10 10"><path d="M2 1l5 4-5 4z" fill="currentColor"/></svg>
      ${icon} <span>${esc(title)}</span>
      <span class="count">${countHtml || ''}</span>
    </div>
    <div class="sec-body"></div>
  </div>`);
  //sec.querySelector('.sec-head').addEventListener('click', () => sec.classList.toggle('collapsed'));
  sec.querySelector('.sec-head').addEventListener('click', () => {
    sec.classList.toggle('collapsed');
    const isCollapsed = sec.classList.contains('collapsed');
    logClick('section "' + title + '" : ' + (isCollapsed ? 'collapsed' : 'expanded'));
  });
  if (bodyNode) sec.querySelector('.sec-body').appendChild(bodyNode);
  root.appendChild(sec);
  return sec;
}

const ic = {
  commit:
    '<svg class="ico" viewBox="0 0 16 16"><circle cx="8" cy="8" r="2.4" fill="none" stroke="#5a6470" stroke-width="1.3"/><path d="M8 1.5v3.6M8 10.9v3.6" stroke="#5a6470" stroke-width="1.3"/></svg>',
  test:
    '<svg class="ico" viewBox="0 0 16 16"><path d="M6 2v4L3 12.5A1 1 0 004 14h8a1 1 0 00.9-1.5L10 6V2" fill="none" stroke="#5a6470" stroke-width="1.2"/><path d="M5 2h6" stroke="#5a6470" stroke-width="1.2"/></svg>',
  cov:
    '<svg class="ico" viewBox="0 0 16 16"><rect x="2" y="2" width="12" height="12" rx="1.5" fill="none" stroke="#5a6470" stroke-width="1.2"/><path d="M2 10l3-3 3 2 4-4" fill="none" stroke="#2f8a2a" stroke-width="1.4"/></svg>',
  warn:
    '<svg class="ico" viewBox="0 0 16 16"><path d="M8 2l6 11H2z" fill="none" stroke="#c4760a" stroke-width="1.2"/><path d="M8 6.5v3.2M8 11.4v.1" stroke="#c4760a" stroke-width="1.3"/></svg>',
  metric:
    '<svg class="ico" viewBox="0 0 16 16"><rect x="2" y="8" width="3" height="6" fill="#5a6470"/><rect x="6.5" y="4" width="3" height="10" fill="#5a6470"/><rect x="11" y="6" width="3" height="8" fill="#5a6470"/></svg>',
  diff:
    '<svg class="ico" viewBox="0 0 16 16"><path d="M4 2h5l3 3v9H4z" fill="none" stroke="#5a6470" stroke-width="1.2"/><path d="M6 9h4M6 11h4" stroke="#5a6470" stroke-width="1.1"/></svg>',
};

// banner
root.appendChild(
  $(`<div class="banner">
  <div class="big">
    <h1>${esc(DATA.repo || 'Assignment')}</h1>
    <div class="sub"><span class="pill muted">Build #${esc(DATA.commitStatusId)}</span></div>
  </div>
  <div class="meta">
    <div class="when">${esc(fmtDate(DATA.createdAt))}</div>
  </div>
</div>`)
);

// metric strip
const lineP = covTot.line.pct;

// previous build, for improvement/degradation deltas
//const prev = (() => {
//  const el = document.getElementById('prev-build');
//  try { return el ? JSON.parse(el.textContent) : null; } catch { return null; }
//})();

const curTestsPass = sStu.pass + sTs.pass;
const curCov = covTot.line.pct;

function deltaBadge(cur, was, unit) {
  if (was == null) return '';
  const d = cur - was;
  if (d === 0) return `<span class="delta same">no change vs prev</span>`;
  const up = d > 0;
  return `<span class="delta ${up ? 'up' : 'down'}">${up ? '\u25B2' : '\u25BC'} ${Math.abs(d)}${unit} vs prev</span>`;
}

root.appendChild(
  $(`<div class="strip">
    <div class="metric"><div class="lbl">Unit Tests</div><div class="val">${curTestsPass}<small> / ${sStu.total + sTs.total}</small></div><div class="note ${sStu.fail + sTs.fail ? 'bad' : 'good'}">${sStu.fail + sTs.fail ? sStu.fail + sTs.fail + ' failing' : 'all passing'}</div></div>
    <div class="metric"><div class="lbl">Line Coverage</div><div class="val" style="color:${covColor(lineP)}">${lineP}<small>%</small></div><div class="note">${covTot.line.cov}/${covTot.line.cov + covTot.line.miss} lines</div></div>
    <div class="metric"><div class="lbl">Static Warnings</div><div class="val">${warnTotal}</div><div class="note ${warnTotal ? 'warn' : 'good'}">${chk.length} checkstyle &middot; ${pmd.length} pmd &middot; ${spot.length} spotbugs</div></div>
    
  </div>`)
);

// <div class="metric"><div class="lbl">Unit Tests</div><div class="val">${curTestsPass}<small> / ${sStu.total + sTs.total}</small></div><div class="note ${sStu.fail + sTs.fail ? 'bad' : 'good'}">${sStu.fail + sTs.fail ? sStu.fail + sTs.fail + ' failing' : 'all passing'}</div><div class="delta-line">${deltaBadge(curTestsPass, prev ? prev.testsPassed : null, '')}</div></div>
// <div class="metric"><div class="lbl">Line Coverage</div><div class="val" style="color:${covColor(lineP)}">${lineP}<small>%</small></div><div class="note">${covTot.line.cov}/${covTot.line.cov + covTot.line.miss} lines</div><div class="delta-line">${deltaBadge(curCov, prev ? prev.lineCoverage : null, '%')}</div></div>
// <div class="metric"><div class="lbl">Place Holder</div></div>

// ---- unit tests ----
function testTable(arr, sectionLabel) {
  const wrap = document.createElement('div');

  // per-section toolbar
  const toolbar = $(`<div class="toolbar">
    <input placeholder="Filter by class or method\u2026">
    <div class="seg">
      <button class="on" data-f="all">All</button>
      <button data-f="pass">Passed</button>
      <button data-f="fail">Failed</button>
    </div>
  </div>`);
  wrap.appendChild(toolbar);

  const tbl = $(`<table><thead><tr><th style="width:22px"></th><th style="width:36px">#</th><th>Class</th><th>Method</th><th style="width:70px">Result</th></tr></thead><tbody></tbody></table>`);
  const tb = tbl.querySelector('tbody');
  // failing first
  const ordered = [...arr].sort((a, b) => (b.fail ? 1 : 0) - (a.fail ? 1 : 0));
  ordered.forEach((t, i) => {
    const f = t.fail;
    const row = $(`<tr class="${f ? 'expandable' : ''} test-row" data-class="${esc(t.className)}" data-method="${esc(t.methodName)}">
      <td class="status-i ${f ? 'f' : 'p'}">${f ? '<span class="chev">\u25B6</span>' : '\u2713'}</td>
      <td class="num" style="color:var(--faint)">${i + 1}</td>
      <td class="cls">${esc(t.className)}</td>
      <td class="mono">${esc(t.methodName)}</td>
      <td><span class="pill ${f ? 'fail' : 'pass'}">${f ? 'fail' : 'pass'}</span></td>
    </tr>`);
    tb.appendChild(row);
    if (f) {
      const det = $(`<tr class="detail" style="display:none"><td colspan="5"><div class="failmsg">${esc(t.failMsg || '(no message)')}</div></td></tr>`);
      tb.appendChild(det);
      row.addEventListener('click', () => {
        const open = det.style.display === 'none';
        det.style.display = open ? '' : 'none';
        row.classList.toggle('open', open);
		logClick(sectionLabel + ' test ' + t.className + '.' + t.methodName + ' : ' + (open ? 'expanded' : 'collapsed'));
      });
    }
  });
  wrap.appendChild(tbl);

  // filtering scoped to THIS section's rows only
  let mode = 'all';
  const input = toolbar.querySelector('input');
  const apply = () => {
    const q = (input.value || '').toLowerCase();
    tb.querySelectorAll('.test-row').forEach((r) => {
      const isFail = r.querySelector('.status-i').classList.contains('f');
      const text = (r.dataset.class + ' ' + r.dataset.method).toLowerCase();
      let show = text.includes(q);
      if (mode === 'pass') show = show && !isFail;
      if (mode === 'fail') show = show && isFail;
      r.style.display = show ? '' : 'none';
      const next = r.nextElementSibling;
      if (next && next.classList.contains('detail') && !show) next.style.display = 'none';
    });
  };
  input.addEventListener('input', apply);
  toolbar.querySelectorAll('.seg button').forEach((b) =>
    b.addEventListener('click', () => {
      toolbar.querySelectorAll('.seg button').forEach((x) => x.classList.remove('on'));
      b.classList.add('on');
      mode = b.dataset.f;
      apply();
	  logClick(sectionLabel + ' filter : ' + b.dataset.f);
    })
  );

  return wrap;
}

const studentBody = document.createElement('div');
studentBody.appendChild(testTable(stu, 'Student'));
const stuSec = section(
  'Student Tests',
  ic.test,
  `<span class="pill pass">${sStu.pass} passed</span> ${sStu.fail ? `<span class="pill fail">${sStu.fail} failed</span>` : ''}`,
  studentBody,
  false
);
stuSec.classList.add('test-section');

const tsBody = document.createElement('div');
tsBody.appendChild(testTable(ts, 'TS'));
const tsSec = section(
  'TS (Instructor) Tests',
  ic.test,
  `<span class="pill pass">${sTs.pass} passed</span> ${sTs.fail ? `<span class="pill fail">${sTs.fail} failed</span>` : ''}`,
  tsBody,
  false
);
tsSec.classList.add('test-section');

// ---- coverage ----
const covWrap = document.createElement('div');
const ov = $(`<div class="toolbar"><b style="font-size:11px">Overall</b>
  <span class="legend" style="margin-left:14px;gap:14px">
    ${['instruction', 'branch', 'line', 'complexity', 'method']
      .map((m) => {
        const o = covTot[m];
        return `<span><i style="background:${covColor(o.pct)}"></i>${m.charAt(0).toUpperCase() + m.slice(1)} <b style="color:${covColor(o.pct)}">${o.pct}%</b></span>`;
      })
      .join('')}
  </span></div>`);
covWrap.appendChild(ov);

const ct = $(`<table><thead><tr><th>Class</th><th>Instructions</th><th>Branches</th><th>Lines</th><th>Complexity</th><th>Methods</th></tr></thead><tbody></tbody></table>`);
const ctb = ct.querySelector('tbody');

function covCell(c, m) {
  const p = pct(c, m);
  return `<td><div class="cov"><div class="track"><div class="fill" style="width:${p}%;background:${covColor(p)}"></div></div><span class="pct" style="color:${covColor(p)}">${p}%</span></div><div class="ratio">${c}/${c + m}</div></td>`;
}

[...cov]
  .sort((a, b) => b.instructionCovered + b.instructionMissed - (a.instructionCovered + a.instructionMissed))
  .forEach((x) => {
    ctb.appendChild(
      $(`<tr><td class="cls">${esc(x.classname)}</td>
    ${covCell(x.instructionCovered, x.instructionMissed)}
    ${covCell(x.branchCovered, x.branchMissed)}
    ${covCell(x.lineCovered, x.lineMissed)}
    ${covCell(x.complexityCovered, x.complexityMissed)}
    ${covCell(x.methodCovered, x.methodMissed)}
  </tr>`)
    );
  });
covWrap.appendChild(ct);
section('Code Coverage', ic.cov, `<span class="pill ${covTot.line.pct >= 90 ? 'pass' : 'warn'}">${covTot.line.pct}% lines</span>`, covWrap, false);

// ---- static analysis ----
const saWrap = document.createElement('div');

function notifTable(title, tag, items, cols, rowfn) {
  saWrap.appendChild(
    $(`<div class="subhead"><span class="tag ${tag}">${title}</span><span class="r"><span class="pill ${items.length ? 'warn' : 'pass'}">${items.length} ${items.length === 1 ? 'issue' : 'issues'}</span></span></div>`)
  );
  if (!items.length) {
    saWrap.appendChild($(`<div class="empty">No ${esc(title)} issues \u2014 clean</div>`));
    return;
  }
  const t = $(`<table><thead><tr>${cols.map((c) => `<th>${c}</th>`).join('')}</tr></thead><tbody></tbody></table>`);
  const tb = t.querySelector('tbody');
  items.forEach((it) => tb.appendChild($(`<tr>${rowfn(it)}</tr>`)));
  saWrap.appendChild(t);
}

notifTable('Checkstyle', 'stu', chk, ['Severity', 'Class', 'Rule', 'Message', 'Line:Col'], (it) =>
  `<td><span class="pill ${it.severity === 'error' ? 'fail' : 'warn'}">${esc(it.severity || 'warn')}</span></td><td class="cls">${esc(it.className)}</td><td class="mono">${esc(it.notification)}</td><td>${esc(it.message)}</td><td class="mono num">${it.line}:${it.col}</td>`
);
notifTable('PMD', 'ts', pmd, ['Priority', 'Class', 'Method', 'Rule', 'Message', 'Line'], (it) =>
  `<td><span class="pill warn">P${it.priority}</span></td><td class="cls">${esc(it.className)}</td><td class="mono">${esc(it.methodName || '')}</td><td class="mono">${esc(it.ruleSet)} / ${esc(it.rule)}</td><td>${esc(it.message)}</td><td class="mono num">${it.beginLine}</td>`
);
notifTable('SpotBugs', 'stu', spot, ['Class', 'Bug', 'Message', 'Line'], (it) =>
  `<td class="cls">${esc(it.className || '')}</td><td class="mono">${esc(it.type || '')}</td><td>${esc(it.message || '')}</td><td class="mono num">${esc(it.line || '')}</td>`
);
section('Static Analysis', ic.warn, `<span class="pill ${warnTotal ? 'warn' : 'pass'}">${warnTotal} total</span>`, saWrap, warnTotal === 0);

// ---- code metrics (counts) ----
const cmt = $(`<table><thead><tr><th>Class</th><th>Type</th><th>Methods</th><th>Asserts</th><th>Code</th><th>Comment</th><th>Blank</th></tr></thead><tbody></tbody></table>`);
const cmtb = cmt.querySelector('tbody');
[...counts]
  .sort((a, b) => b.ncode - a.ncode)
  .forEach((x) => {
    cmtb.appendChild(
      $(`<tr><td class="cls">${esc(x.classname)}</td>
    <td><span class="pill ${x.srcType === 'src' ? 'info' : 'muted'}">${esc(x.srcType)}</span></td>
    <td class="num">${x.methods}</td><td class="num">${x.asserts}</td><td class="num">${x.ncode}</td>
    <td class="num">${x.ncomment}</td><td class="num">${x.nblank}</td></tr>`)
    );
  });
const totCode = counts.reduce((a, x) => a + x.ncode, 0);
const totSrc = counts.filter((x) => x.srcType === 'src').length;
const totTest = counts.filter((x) => x.srcType === 'test').length;
const cmSec = section('Code Metrics', ic.metric, `<span class="pill muted">${totSrc} src &middot; ${totTest} test &middot; ${totCode} LOC</span>`, cmt, true);
cmSec.classList.add('code-metrics');

// ---- changed files ----
const dfBody = document.createElement('div');
if (diffs.length) {
  const t = $(`<table><thead><tr><th style="width:34px"></th><th>Path</th><th style="width:130px">Changes</th></tr></thead><tbody></tbody></table>`);
  const tb = t.querySelector('tbody');
  const tmap = { M: ['info', 'M'], A: ['pass', 'A'], D: ['fail', 'D'] };
  diffs.forEach((d) => {
    const tm = tmap[d.commitType] || ['muted', d.commitType];
    tb.appendChild(
      $(`<tr><td><span class="pill ${tm[0]}">${tm[1]}</span></td><td class="mono">${esc(d.path)}</td>
      <td><span style="color:var(--pass)">+${d.insertions}</span> / <span style="color:var(--fail)">-${d.deletions}</span></td></tr>`)
    );
  });
  dfBody.appendChild(t);
} else {
  dfBody.appendChild($(`<div class="empty">No file changes recorded</div>`));
}
section('Changed Files', ic.diff, `<span class="pill muted">${diffs.length} file(s)</span>`, dfBody, true);

// ---- commit details ----
const cdBody = $(`<table><tbody>
  <tr><td style="color:var(--muted);width:130px">Repository</td><td class="mono">${esc(DATA.repo || '')}</td></tr>
  <tr><td style="color:var(--muted)">Commit</td><td class="mono">${esc(DATA.commitHash || '')}</td></tr>
  <tr><td style="color:var(--muted)">Author</td><td>${esc(cd.authorName || '')} &lt;${esc(cd.authorEmail || '')}&gt;</td></tr>
  <tr><td style="color:var(--muted)">Message</td><td>${esc(cd.commitMessage || '')}</td></tr>
  <tr><td style="color:var(--muted)">Changes</td><td><span class="pill pass">+${cd.insertions || 0}</span> <span class="pill fail">-${cd.deletions || 0}</span> &nbsp;${cd.linesChanged || 0} lines &middot; ${cd.files || 0} file(s)</td></tr>
  <tr><td style="color:var(--muted)">Processed</td><td>${fmtDate(DATA.createdAt)}</td></tr>
</tbody></table>`);
section('Commit Details', ic.commit, `<span class="pill muted">${esc((DATA.commitHash || '').slice(0, 7))}</span>`, cdBody, false);


// ---------- interactions ----------
function setAll(collapse) {
  document.querySelectorAll('.section').forEach((s) => s.classList.toggle('collapsed', collapse));
}


// wire up title-bar tools and test toolbar (replaces former inline handlers)
//document.querySelector('[data-action="collapse-all"]').addEventListener('click', () => setAll(true));
//document.querySelector('[data-action="expand-all"]').addEventListener('click', () => setAll(false));

document.querySelector('[data-action="collapse-all"]').addEventListener('click', () => {
  setAll(true);
  logClick('collapse all');
});
document.querySelector('[data-action="expand-all"]').addEventListener('click', () => {
  setAll(false);
  logClick('expand all');
});

function setTheme(t) {
  document.documentElement.setAttribute('data-theme', t);
  try { localStorage.setItem('gradeReportTheme', t); } catch (e) { /* SWT sandbox */ }
}

const themeBtn = document.querySelector('[data-action="toggle-theme"]');
if (themeBtn) {
  const flip = () => {
    const next = document.documentElement.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
    setTheme(next);
    logClick('theme : ' + next);
  };
  themeBtn.addEventListener('click', flip);
  themeBtn.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); flip(); }
  });
}

// ---------- click logging ----------
function logClick(msg) {
  console.log('[click] ' + msg);          // browser DevTools (standalone)
  if (window.javaLog) window.javaLog(msg); // Eclipse console (SWT Browser)
}