const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = process.env.PORT || 3000;

const htmlContent = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>BudgetFlow - Your money, your story.</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Rounded:opsz,wght,FILL,GRAD@24,400,1,0" />
  <style>
    :root {
      --bg: #0F172A;
      --card-bg: #1E293B;
      --card-subtle: #243247;
      --text: #F1F5F9;
      --text-muted: #94A3B8;
      --primary: #10B981;
      --primary-light: #34D399;
      --primary-dim: rgba(16, 185, 129, 0.15);
      --expense: #EF4444;
      --expense-dim: rgba(239, 68, 68, 0.15);
      --income: #10B981;
      --income-dim: rgba(16, 185, 129, 0.15);
      --warning: #F59E0B;
      --border: rgba(148, 163, 184, 0.15);
      --font: 'Plus Jakarta Sans', -apple-system, sans-serif;
    }

    [data-theme="light"] {
      --bg: #F8FAFC;
      --card-bg: #FFFFFF;
      --card-subtle: #F1F5F9;
      --text: #0F172A;
      --text-muted: #64748B;
      --border: rgba(100, 116, 139, 0.15);
    }

    * { box-sizing: border-box; margin: 0; padding: 0; -webkit-tap-highlight-color: transparent; }
    body {
      font-family: var(--font);
      background: #0B1120;
      color: var(--text);
      display: flex;
      justify-content: center;
      min-height: 100vh;
      overflow-x: hidden;
    }

    .phone-container {
      width: 100%;
      max-width: 440px;
      height: 100vh;
      background: var(--bg);
      display: flex;
      flex-direction: column;
      position: relative;
      overflow: hidden;
      box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.7);
    }

    @media (min-width: 460px) {
      body { padding: 16px 0; }
      .phone-container { height: calc(100vh - 32px); border-radius: 36px; border: 4px solid #1E293B; }
    }

    .header {
      padding: 16px 20px 10px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      background: var(--bg);
      z-index: 10;
    }
    .logo-title {
      font-size: 20px;
      font-weight: 800;
      color: var(--primary-light);
      letter-spacing: -0.5px;
    }
    .logo-subtitle {
      font-size: 13px;
      color: var(--text-muted);
      font-weight: 500;
    }
    .header-actions { display: flex; gap: 8px; }
    .icon-btn {
      width: 38px;
      height: 38px;
      border-radius: 12px;
      border: none;
      background: var(--card-bg);
      color: var(--text);
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.2s;
    }
    .icon-btn:hover { background: var(--card-subtle); color: var(--primary-light); }

    .screen-content {
      flex: 1;
      overflow-y: auto;
      padding: 0 16px 90px;
      scroll-behavior: smooth;
    }

    .card {
      background: var(--card-bg);
      border-radius: 20px;
      padding: 18px;
      margin-bottom: 14px;
      border: 1px solid var(--border);
    }

    .balance-card {
      background: linear-gradient(135deg, #1E293B 0%, #172033 100%);
      border: 1px solid rgba(16, 185, 129, 0.25);
      position: relative;
      overflow: hidden;
    }
    .balance-card::after {
      content: '';
      position: absolute;
      top: -40px;
      right: -40px;
      width: 120px;
      height: 120px;
      background: radial-gradient(circle, rgba(16, 185, 129, 0.15) 0%, transparent 70%);
      pointer-events: none;
    }
    .balance-label { font-size: 12px; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.5px; font-weight: 600; }
    .balance-amount { font-size: 32px; font-weight: 800; margin: 4px 0 14px; letter-spacing: -1px; }
    .stat-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; padding-top: 12px; border-top: 1px solid var(--border); }
    .stat-item { display: flex; flex-direction: column; }
    .stat-badge {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      font-size: 11px;
      font-weight: 600;
      margin-bottom: 4px;
    }
    .stat-badge.income { color: var(--income); }
    .stat-badge.expense { color: var(--expense); }
    .stat-val { font-size: 17px; font-weight: 700; }

    .budget-progress { margin-top: 12px; }
    .progress-bar-bg { width: 100%; height: 7px; background: rgba(255, 255, 255, 0.08); border-radius: 99px; overflow: hidden; margin-top: 5px; }
    .progress-bar-fill { height: 100%; border-radius: 99px; transition: width 0.4s ease; }

    .quick-chips { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 8px; margin-bottom: 16px; }
    .chip-btn {
      background: var(--card-bg);
      border: 1px solid var(--border);
      border-radius: 14px;
      padding: 10px 6px;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;
      color: var(--text);
      font-size: 12px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s;
    }
    .chip-btn:hover { background: var(--card-subtle); border-color: var(--primary); }
    .chip-btn .material-symbols-rounded { font-size: 20px; }
    .chip-btn.exp .material-symbols-rounded { color: var(--expense); }
    .chip-btn.inc .material-symbols-rounded { color: var(--income); }
    .chip-btn.not .material-symbols-rounded { color: #60A5FA; }

    .section-title {
      font-size: 15px;
      font-weight: 700;
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin: 16px 0 10px;
    }
    .section-title a {
      font-size: 12px;
      color: var(--primary-light);
      text-decoration: none;
      cursor: pointer;
    }

    .tx-item {
      display: flex;
      align-items: center;
      padding: 12px 14px;
      background: var(--card-bg);
      border: 1px solid var(--border);
      border-radius: 16px;
      margin-bottom: 8px;
      cursor: pointer;
      transition: transform 0.15s, background 0.15s;
    }
    .tx-item:hover { transform: translateY(-1px); background: var(--card-subtle); }
    .cat-icon-badge {
      width: 42px;
      height: 42px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 12px;
      font-size: 20px;
    }
    .tx-info { flex: 1; min-width: 0; }
    .tx-cat { font-size: 14px; font-weight: 700; color: var(--text); }
    .tx-sub { font-size: 11px; color: var(--text-muted); margin-top: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .tx-amount { font-size: 17px; font-weight: 800; text-align: right; }
    .tx-amount.expense { color: var(--expense); }
    .tx-amount.income { color: var(--income); }

    .bottom-nav {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 70px;
      background: var(--card-bg);
      border-top: 1px solid var(--border);
      display: flex;
      justify-content: space-around;
      align-items: center;
      z-index: 20;
    }
    .nav-btn {
      background: none;
      border: none;
      color: var(--text-muted);
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 3px;
      font-size: 11px;
      font-weight: 600;
      cursor: pointer;
      width: 60px;
      transition: color 0.2s;
    }
    .nav-btn.active { color: var(--primary-light); }
    .nav-btn .material-symbols-rounded { font-size: 22px; }
    .nav-fab {
      width: 52px;
      height: 52px;
      border-radius: 50%;
      background: var(--primary);
      border: none;
      color: #FFF;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      box-shadow: 0 10px 20px rgba(16, 185, 129, 0.4);
      margin-top: -24px;
      transition: transform 0.15s, background 0.15s;
    }
    .nav-fab:hover { transform: scale(1.05); background: var(--primary-light); }

    .modal-overlay {
      position: absolute;
      inset: 0;
      background: rgba(0, 0, 0, 0.7);
      backdrop-filter: blur(4px);
      display: none;
      align-items: flex-end;
      z-index: 50;
      animation: fadeIn 0.2s ease;
    }
    .modal-sheet {
      width: 100%;
      max-height: 90%;
      background: var(--card-bg);
      border-radius: 28px 28px 0 0;
      padding: 20px;
      overflow-y: auto;
      border-top: 1px solid var(--border);
    }
    .form-group { margin-bottom: 14px; }
    .form-label { font-size: 12px; font-weight: 700; color: var(--text-muted); margin-bottom: 6px; display: block; }
    .form-input {
      width: 100%;
      background: var(--bg);
      border: 1px solid var(--border);
      border-radius: 12px;
      padding: 12px 14px;
      color: var(--text);
      font-size: 15px;
      font-family: inherit;
    }
    .form-input:focus { outline: none; border-color: var(--primary); }
    .type-toggle { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 16px; }
    .toggle-btn {
      padding: 10px;
      border-radius: 12px;
      border: none;
      background: var(--bg);
      color: var(--text-muted);
      font-weight: 700;
      cursor: pointer;
    }
    .toggle-btn.active.exp { background: var(--expense); color: #FFF; }
    .toggle-btn.active.inc { background: var(--income); color: #FFF; }
    .cat-chips-grid { display: flex; flex-wrap: wrap; gap: 6px; max-height: 120px; overflow-y: auto; padding-bottom: 4px; }
    .cat-chip {
      padding: 6px 12px;
      border-radius: 10px;
      background: var(--bg);
      border: 1px solid var(--border);
      font-size: 12px;
      font-weight: 600;
      cursor: pointer;
      color: var(--text);
    }
    .cat-chip.active { background: var(--primary-dim); border-color: var(--primary); color: var(--primary-light); }
    .subcat-chips-grid { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 4px; }
    .subcat-chip {
      padding: 4px 10px;
      border-radius: 8px;
      background: var(--card-subtle);
      border: 1px solid var(--border);
      font-size: 11px;
      font-weight: 600;
      cursor: pointer;
      color: var(--text-muted);
    }
    .subcat-chip.active { background: var(--primary); border-color: var(--primary); color: #FFF; }
    .btn-submit {
      width: 100%;
      padding: 14px;
      border-radius: 14px;
      border: none;
      background: var(--primary);
      color: #FFF;
      font-size: 16px;
      font-weight: 800;
      cursor: pointer;
      margin-top: 12px;
    }

    /* Enhanced Calendar Grid with Actual Amounts */
    .cal-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 4px; }
    .cal-header-day { font-size: 11px; font-weight: 700; color: var(--text-muted); text-align: center; padding: 4px 0; }
    .cal-day {
      min-height: 52px;
      border-radius: 10px;
      background: var(--card-bg);
      border: 1px solid var(--border);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: flex-start;
      padding: 4px 2px;
      cursor: pointer;
      position: relative;
      transition: background 0.15s;
    }
    .cal-day:hover { background: var(--card-subtle); }
    .cal-day.today { border-color: var(--primary); border-width: 1.5px; }
    .cal-day-num { font-size: 11px; font-weight: 700; color: var(--text); }
    .cal-day.today .cal-day-num { color: var(--primary-light); }
    .cal-day-amount {
      font-size: 9px;
      font-weight: 800;
      margin-top: 3px;
      text-align: center;
      letter-spacing: -0.2px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      width: 100%;
      padding: 0 1px;
    }
    .cal-day-amount.exp { color: var(--expense); }
    .cal-day-amount.inc { color: var(--income); }

    .snackbar {
      position: absolute;
      bottom: 80px;
      left: 20px;
      right: 20px;
      background: #334155;
      color: #FFF;
      padding: 12px 16px;
      border-radius: 12px;
      display: none;
      justify-content: space-between;
      align-items: center;
      z-index: 100;
      font-size: 13px;
      font-weight: 600;
      box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.4);
    }
    .undo-btn { background: none; border: none; color: var(--primary-light); font-weight: 800; cursor: pointer; }

    @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
  </style>
</head>
<body>
  <div class="phone-container">
    <div class="header">
      <div>
        <div class="logo-title">BudgetFlow</div>
        <div class="logo-subtitle" id="current-month-label">October 2026</div>
      </div>
      <div class="header-actions">
        <button class="icon-btn" onclick="openReports()" title="Reports"><span class="material-symbols-rounded">bar_chart</span></button>
        <button class="icon-btn" onclick="openSavings()" title="Savings Goals"><span class="material-symbols-rounded">savings</span></button>
        <button class="icon-btn" onclick="openSettings()" title="Settings"><span class="material-symbols-rounded">settings</span></button>
      </div>
    </div>

    <div class="screen-content" id="screen-container"></div>

    <div class="bottom-nav">
      <button class="nav-btn active" id="tab-home" onclick="switchTab('home')">
        <span class="material-symbols-rounded">home</span>
        Home
      </button>
      <button class="nav-btn" id="tab-calendar" onclick="switchTab('calendar')">
        <span class="material-symbols-rounded">calendar_month</span>
        Calendar
      </button>
      <button class="nav-fab" onclick="openAddModal('expense')" title="Add Transaction">
        <span class="material-symbols-rounded" style="font-size:28px">add</span>
      </button>
      <button class="nav-btn" id="tab-notes" onclick="switchTab('notes')">
        <span class="material-symbols-rounded">menu_book</span>
        Notes
      </button>
      <button class="nav-btn" id="tab-budget" onclick="switchTab('budget')">
        <span class="material-symbols-rounded">pie_chart</span>
        Budget
      </button>
    </div>

    <!-- Add/Edit Transaction Modal -->
    <div class="modal-overlay" id="add-modal">
      <div class="modal-sheet">
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:14px;">
          <h3 id="modal-title" style="font-size:18px; font-weight:800;">Add Transaction</h3>
          <button class="icon-btn" onclick="closeModal('add-modal')"><span class="material-symbols-rounded">close</span></button>
        </div>

        <div class="type-toggle">
          <button class="toggle-btn active exp" id="btn-toggle-exp" onclick="setType('expense')">Expense</button>
          <button class="toggle-btn inc" id="btn-toggle-inc" onclick="setType('income')">Income</button>
        </div>

        <div class="form-group">
          <label class="form-label">AMOUNT (POSITIVE DECIMAL)</label>
          <input type="number" step="0.01" class="form-input" id="inp-amount" placeholder="0.00" style="font-size:22px; font-weight:800;" />
        </div>

        <div class="form-group">
          <label class="form-label">CATEGORY</label>
          <div class="cat-chips-grid" id="cat-chips-list"></div>
        </div>

        <!-- Subcategories Section -->
        <div class="form-group">
          <label class="form-label">SUBCATEGORY (OPTIONAL)</label>
          <div class="subcat-chips-grid" id="subcat-chips-list"></div>
        </div>

        <div class="form-group">
          <label class="form-label">DATE</label>
          <input type="date" class="form-input" id="inp-date" />
        </div>

        <div class="form-group">
          <label class="form-label">NOTE (MAX 500 CHARACTERS)</label>
          <input type="text" maxlength="500" class="form-input" id="inp-note" placeholder="Merchant, context or intention..." />
        </div>

        <div class="form-group">
          <label class="form-label">PAYMENT METHOD</label>
          <div style="display:flex; gap:8px;">
            <button type="button" class="cat-chip active" id="pm-card" onclick="setPM('card')">Card</button>
            <button type="button" class="cat-chip" id="pm-cash" onclick="setPM('cash')">Cash</button>
            <button type="button" class="cat-chip" id="pm-transfer" onclick="setPM('transfer')">Transfer</button>
          </div>
        </div>

        <button class="btn-submit" onclick="submitTransaction()">Save Transaction</button>
      </div>
    </div>

    <!-- Generic Dialog / Sheet Modal -->
    <div class="modal-overlay" id="detail-modal">
      <div class="modal-sheet" id="detail-modal-body"></div>
    </div>

    <!-- Undo Snackbar -->
    <div class="snackbar" id="snackbar">
      <span id="snackbar-msg">Transaction deleted</span>
      <button class="undo-btn" onclick="undoDelete()">UNDO</button>
    </div>
  </div>

  <script>
    const subcategoryPresets = {
      'Food': ['Groceries', 'Restaurants', 'Coffee & Cafes', 'Food Delivery', 'Alcohol & Bars'],
      'Transport': ['Fuel / Petrol', 'Public Transit', 'Rideshare / Taxi', 'Parking', 'Maintenance'],
      'Housing': ['Rent', 'Mortgage', 'Property Tax', 'Repairs', 'Furniture'],
      'Utilities': ['Electricity', 'Water', 'Internet', 'Mobile Phone', 'Gas', 'Trash'],
      'Entertainment': ['Streaming', 'Movies & Concerts', 'Gaming', 'Hobbies', 'Books'],
      'Health': ['Doctor & Dentist', 'Pharmacy', 'Fitness & Gym', 'Insurance', 'Therapy'],
      'Shopping': ['Clothing', 'Electronics', 'Personal Care', 'Home Essentials', 'Gifts'],
      'Education': ['Courses & Tuition', 'Books', 'Workshops', 'Certifications'],
      'Savings': ['Emergency Fund', 'Retirement', 'Investments', 'Vacation Goal'],
      'Salary': ['Base Salary', 'Bonus', 'Overtime', 'Commission'],
      'Freelance': ['Client Projects', 'Consulting', 'Side Business'],
      'Investment': ['Dividends', 'Capital Gains', 'Interest', 'Rental Yield'],
      'Other': ['General', 'Cash Gift', 'Reimbursement', 'Miscellaneous']
    };

    const STORE_KEY = 'budgetflow_data_v2';
    let state = JSON.parse(localStorage.getItem(STORE_KEY)) || {
      currency: 'MYR',
      theme: 'dark',
      startOfWeek: 'sunday',
      budgetCycleDay: 1,
      overallBudgetCents: 250000,
      categories: [
        { name: 'Food', icon: 'restaurant', color: '#EF4444' },
        { name: 'Transport', icon: 'directions_car', color: '#3B82F6' },
        { name: 'Housing', icon: 'home', color: '#8B5CF6' },
        { name: 'Utilities', icon: 'bolt', color: '#F59E0B' },
        { name: 'Entertainment', icon: 'movie', color: '#EC4899' },
        { name: 'Health', icon: 'local_hospital', color: '#10B981' },
        { name: 'Shopping', icon: 'shopping_bag', color: '#06B6D4' },
        { name: 'Education', icon: 'school', color: '#6366F1' },
        { name: 'Savings', icon: 'savings', color: '#14B8A6' },
        { name: 'Salary', icon: 'payments', color: '#10B981' },
        { name: 'Freelance', icon: 'work', color: '#3B82F6' },
        { name: 'Investment', icon: 'trending_up', color: '#8B5CF6' },
        { name: 'Other', icon: 'category', color: '#64748B' }
      ],
      budgets: {
        'Food': 60000,
        'Transport': 30000,
        'Entertainment': 20000,
        'Housing': 80000
      },
      transactions: [
        { id: 1, amountCents: 350000, type: 'income', category: 'Salary', subcategory: 'Base Salary', date: '2026-10-01', note: 'Monthly primary salary', paymentMethod: 'transfer', isDeleted: false },
        { id: 2, amountCents: 80000, type: 'expense', category: 'Housing', subcategory: 'Rent', date: '2026-10-02', note: 'Monthly apartment rent', paymentMethod: 'transfer', isDeleted: false },
        { id: 3, amountCents: 1250, type: 'expense', category: 'Food', subcategory: 'Coffee & Cafes', date: '2026-10-08', note: 'Morning breakfast & coffee', paymentMethod: 'card', isDeleted: false },
        { id: 4, amountCents: 4500, type: 'expense', category: 'Food', subcategory: 'Groceries', date: '2026-10-07', note: 'Weekly groceries market', paymentMethod: 'card', isDeleted: false },
        { id: 5, amountCents: 1800, type: 'expense', category: 'Transport', subcategory: 'Public Transit', date: '2026-10-06', note: 'Train commute card top-up', paymentMethod: 'card', isDeleted: false }
      ],
      recurring: [
        { id: 1, name: 'High-speed Internet', amountCents: 12000, type: 'expense', category: 'Utilities', frequency: 'monthly', nextDueDate: '2026-10-28', dayOfMonth: 28 },
        { id: 2, name: 'Apartment Rent', amountCents: 80000, type: 'expense', category: 'Housing', frequency: 'monthly', nextDueDate: '2026-11-01', dayOfMonth: 1 }
      ],
      journal: [
        { id: 1, title: 'Budgeting with intentionality', body: 'Started the month tracking every purchase with BudgetFlow. Categorizing with subcategories makes every penny intentional.', date: '2026-10-08', mood: 'proud', tags: 'milestone,goals', isPinned: true, isDeleted: false }
      ],
      savingsGoals: [
        { id: 1, name: 'Emergency Fund', targetAmountCents: 500000, currentAmountCents: 150000, targetDate: '2027-04-01' }
      ]
    };

    function saveState() {
      localStorage.setItem(STORE_KEY, JSON.stringify(state));
    }

    let currentTab = 'home';
    let currentType = 'expense';
    let selectedCat = 'Food';
    let selectedSubcat = null;
    let selectedPM = 'card';
    let lastDeletedTx = null;

    function formatCents(cents, code = state.currency) {
      const sym = code === 'MYR' ? 'RM' : (code === 'USD' ? '$' : (code === 'EUR' ? '€' : code));
      const dollars = (Math.abs(cents) / 100).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
      return (cents < 0 ? '-' : '') + sym + ' ' + dollars;
    }

    function formatDayCompact(cents) {
      const sym = state.currency === 'MYR' ? 'RM' : (state.currency === 'USD' ? '$' : '€');
      const absDollars = Math.abs(cents) / 100;
      if (absDollars >= 1000) {
        return sym + (absDollars / 1000).toFixed(1) + 'k';
      }
      return sym + Math.round(absDollars);
    }

    function switchTab(tab) {
      currentTab = tab;
      document.querySelectorAll('.nav-btn').forEach(btn => btn.classList.remove('active'));
      const activeBtn = document.getElementById('tab-' + tab);
      if (activeBtn) activeBtn.classList.add('active');
      renderScreen();
    }

    function renderScreen() {
      const container = document.getElementById('screen-container');
      if (currentTab === 'home') renderHome(container);
      else if (currentTab === 'calendar') renderCalendar(container);
      else if (currentTab === 'notes') renderNotes(container);
      else if (currentTab === 'budget') renderBudget(container);
    }

    function renderHome(container) {
      const activeTxs = state.transactions.filter(t => !t.isDeleted);
      const spent = activeTxs.filter(t => t.type === 'expense').reduce((acc, t) => acc + t.amountCents, 0);
      const income = activeTxs.filter(t => t.type === 'income').reduce((acc, t) => acc + t.amountCents, 0);
      const net = income - spent;
      const budgetLimit = state.overallBudgetCents;
      const progress = Math.min(100, Math.round((spent / budgetLimit) * 100));
      const isOver = spent > budgetLimit;

      let html = \`
        <div class="card balance-card">
          <div class="balance-label">Net Balance (October)</div>
          <div class="balance-amount" style="color: \${net >= 0 ? 'var(--text)' : 'var(--expense)'}">\${formatCents(net)}</div>
          <div class="stat-row">
            <div class="stat-item">
              <span class="stat-badge income"><span class="material-symbols-rounded" style="font-size:14px">arrow_downward</span> INCOME</span>
              <span class="stat-val" style="color:var(--income)">\${formatCents(income)}</span>
            </div>
            <div class="stat-item">
              <span class="stat-badge expense"><span class="material-symbols-rounded" style="font-size:14px">arrow_upward</span> SPENT</span>
              <span class="stat-val" style="color:var(--expense)">\${formatCents(spent)}</span>
            </div>
          </div>
          <div class="budget-progress">
            <div style="display:flex; justify-content:space-between; font-size:11px; color:var(--text-muted); font-weight:600;">
              <span>Monthly Budget: \${formatCents(budgetLimit)}</span>
              <span style="color:\${isOver ? 'var(--expense)' : (progress >= 80 ? 'var(--warning)' : 'var(--income)')}">
                \${isOver ? 'Over by ' + formatCents(spent - budgetLimit) : formatCents(budgetLimit - spent) + ' left (' + progress + '%)'}
              </span>
            </div>
            <div class="progress-bar-bg">
              <div class="progress-bar-fill" style="width:\${progress}%; background:\${isOver ? 'var(--expense)' : (progress >= 80 ? 'var(--warning)' : 'var(--income)')}"></div>
            </div>
          </div>
        </div>

        <div class="quick-chips">
          <button class="chip-btn exp" onclick="openAddModal('expense')">
            <span class="material-symbols-rounded">add</span>
            Add Expense
          </button>
          <button class="chip-btn inc" onclick="openAddModal('income')">
            <span class="material-symbols-rounded">add</span>
            Add Income
          </button>
          <button class="chip-btn not" onclick="openWriteNoteModal()">
            <span class="material-symbols-rounded">edit_note</span>
            Write Note
          </button>
        </div>

        <div class="section-title">
          <span>Upcoming Bills</span>
          <a onclick="openRecurringManage()">Manage</a>
        </div>
      \`;

      state.recurring.slice(0, 3).forEach(b => {
        html += \`
          <div class="tx-item" onclick="recordBill(\${b.id})">
            <div class="cat-icon-badge" style="background:rgba(245, 158, 11, 0.15); color:#F59E0B">
              <span class="material-symbols-rounded">repeat</span>
            </div>
            <div class="tx-info">
              <div class="tx-cat">\${b.name}</div>
              <div class="tx-sub">Due \${b.nextDueDate} • \${b.frequency}</div>
            </div>
            <div style="text-align:right;">
              <div class="tx-amount">\${formatCents(b.amountCents)}</div>
              <button style="padding:4px 8px; border-radius:8px; border:none; background:var(--primary); color:#FFF; font-size:10px; font-weight:700; cursor:pointer; margin-top:2px;">PAID</button>
            </div>
          </div>
        \`;
      });

      html += \`
        <div class="section-title">
          <span>Recent Transactions</span>
          <a onclick="openAllTransactions()">See All</a>
        </div>
      \`;

      activeTxs.slice(0, 8).forEach(tx => {
        const cat = state.categories.find(c => c.name === tx.category) || { color: '#64748B', icon: 'category' };
        html += \`
          <div class="tx-item" onclick="openTxDetail(\${tx.id})">
            <div class="cat-icon-badge" style="background:\${cat.color}22; color:\${cat.color}">
              <span class="material-symbols-rounded">\${cat.icon}</span>
            </div>
            <div class="tx-info">
              <div class="tx-cat">\${tx.category} \${tx.subcategory ? '<span style="font-weight:400; color:var(--text-muted); font-size:12px;">• ' + tx.subcategory + '</span>' : ''}</div>
              <div class="tx-sub">\${tx.note ? tx.note + ' • ' : ''}\${tx.date}</div>
            </div>
            <div class="tx-amount \${tx.type}">
              \${tx.type === 'expense' ? '-' : '+'}\${formatCents(tx.amountCents)}
            </div>
          </div>
        \`;
      });

      container.innerHTML = html;
    }

    // Calendar Screen with Actual Amount on Days
    function renderCalendar(container) {
      let html = \`
        <div class="section-title" style="margin-top:4px;">
          <span>Monthly Grid (October 2026)</span>
          <span style="font-size:12px; color:var(--text-muted)">Actual amounts shown</span>
        </div>
        <div class="cal-grid" style="margin-bottom:6px;">
          <div class="cal-header-day">Sun</div>
          <div class="cal-header-day">Mon</div>
          <div class="cal-header-day">Tue</div>
          <div class="cal-header-day">Wed</div>
          <div class="cal-header-day">Thu</div>
          <div class="cal-header-day">Fri</div>
          <div class="cal-header-day">Sat</div>
        </div>
        <div class="cal-grid">
      \`;

      // Oct 2026 starts on Thursday (index 4)
      for (let i = 0; i < 4; i++) {
        html += \`<div></div>\`;
      }

      for (let day = 1; day <= 31; day++) {
        const dStr = '2026-10-' + (day < 10 ? '0' + day : day);
        const dayTxs = state.transactions.filter(t => !t.isDeleted && t.date === dStr);
        const dayExp = dayTxs.filter(t => t.type === 'expense').reduce((acc, t) => acc + t.amountCents, 0);
        const dayInc = dayTxs.filter(t => t.type === 'income').reduce((acc, t) => acc + t.amountCents, 0);

        let amountBadge = '';
        if (dayExp > 0 && dayInc === 0) {
          amountBadge = \`<span class="cal-day-amount exp">-\${formatDayCompact(dayExp)}</span>\`;
        } else if (dayInc > 0 && dayExp === 0) {
          amountBadge = \`<span class="cal-day-amount inc">+\${formatDayCompact(dayInc)}</span>\`;
        } else if (dayExp > 0 && dayInc > 0) {
          const net = dayInc - dayExp;
          const cls = net >= 0 ? 'inc' : 'exp';
          const sign = net >= 0 ? '+' : '-';
          amountBadge = \`<span class="cal-day-amount \${cls}">\${sign}\${formatDayCompact(net)}</span>\`;
        }

        const isToday = day === 8;
        html += \`
          <div class="cal-day \${isToday ? 'today' : ''}" onclick="openDayDetail('\${dStr}')">
            <span class="cal-day-num">\${day}</span>
            \${amountBadge}
          </div>
        \`;
      }

      html += \`</div>
        <div class="card" style="margin-top:16px;">
          <div style="font-size:13px; font-weight:700; margin-bottom:6px;">Calendar Insights</div>
          <div style="font-size:12px; color:var(--text-muted); line-height:1.5;">
            Every day displays the <strong>actual amount spent or earned</strong>. Tap any day to inspect individual transactions or log an expense.
          </div>
        </div>
      \`;
      container.innerHTML = html;
    }

    function renderNotes(container) {
      let html = \`
        <div class="section-title" style="margin-top:4px;">
          <span>Financial Reflections & Notes</span>
          <a onclick="openWriteNoteModal()">+ New Note</a>
        </div>
      \`;

      const activeNotes = state.journal.filter(n => !n.isDeleted);
      if (activeNotes.length === 0) {
        html += \`<div style="text-align:center; padding:32px; color:var(--text-muted);">No reflection notes yet. Tap + New Note above.</div>\`;
      } else {
        activeNotes.forEach(n => {
          html += \`
            <div class="card" style="margin-bottom:10px;">
              <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px;">
                <span style="font-size:11px; color:var(--primary-light); font-weight:700; text-transform:uppercase;">Mood: \${n.mood || 'Reflective'}</span>
                <span style="font-size:11px; color:var(--text-muted)">\${n.date}</span>
              </div>
              <div style="font-size:15px; font-weight:700; margin-bottom:4px;">\${n.title || 'Untitled Note'}</div>
              <div style="font-size:13px; color:var(--text-muted); line-height:1.4;">\${n.body}</div>
              \${n.tags ? \`<div style="margin-top:8px; font-size:11px; color:var(--primary-light);">#\${n.tags.split(',').join(' #')}</div>\` : ''}
            </div>
          \`;
        });
      }
      container.innerHTML = html;
    }

    function renderBudget(container) {
      const activeTxs = state.transactions.filter(t => !t.isDeleted && t.type === 'expense');
      let html = \`
        <div class="section-title" style="margin-top:4px;">
          <span>Category Budgets</span>
          <span style="font-size:12px; color:var(--text-muted)">Monthly Limits</span>
        </div>
      \`;

      Object.keys(state.budgets).forEach(catName => {
        const limit = state.budgets[catName];
        const spent = activeTxs.filter(t => t.category === catName).reduce((acc, t) => acc + t.amountCents, 0);
        const pct = Math.min(100, Math.round((spent / limit) * 100));
        const isOver = spent > limit;
        const color = isOver ? 'var(--expense)' : (pct >= 80 ? 'var(--warning)' : 'var(--income)');

        html += \`
          <div class="card" style="margin-bottom:10px;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:4px;">
              <span style="font-size:14px; font-weight:700;">\${catName}</span>
              <span style="font-size:13px; font-weight:700; color:\${color}">\${formatCents(spent)} / \${formatCents(limit)}</span>
            </div>
            <div class="progress-bar-bg">
              <div class="progress-bar-fill" style="width:\${pct}%; background:\${color}"></div>
            </div>
            <div style="display:flex; justify-content:space-between; font-size:11px; color:var(--text-muted); margin-top:6px;">
              <span>\${pct}% used</span>
              <span>\${isOver ? 'Over budget!' : formatCents(limit - spent) + ' remaining'}</span>
            </div>
          </div>
        \`;
      });
      container.innerHTML = html;
    }

    function openAddModal(type = 'expense') {
      currentType = type;
      selectedSubcat = null;
      document.getElementById('modal-title').textContent = type === 'expense' ? 'Add Expense' : 'Add Income';
      setType(type);
      document.getElementById('inp-amount').value = '';
      document.getElementById('inp-date').value = '2026-10-08';
      document.getElementById('inp-note').value = '';
      renderCatChips();
      renderSubcatChips();
      document.getElementById('add-modal').style.display = 'flex';
    }

    function closeModal(id) {
      document.getElementById(id).style.display = 'none';
    }

    function setType(type) {
      currentType = type;
      document.getElementById('btn-toggle-exp').className = 'toggle-btn ' + (type === 'expense' ? 'active exp' : '');
      document.getElementById('btn-toggle-inc').className = 'toggle-btn ' + (type === 'income' ? 'active inc' : '');
      selectedCat = type === 'income' ? 'Salary' : 'Food';
      selectedSubcat = null;
      renderCatChips();
      renderSubcatChips();
    }

    function renderCatChips() {
      const list = document.getElementById('cat-chips-list');
      list.innerHTML = '';
      state.categories.forEach(c => {
        const chip = document.createElement('button');
        chip.type = 'button';
        chip.className = 'cat-chip ' + (selectedCat === c.name ? 'active' : '');
        chip.textContent = c.name;
        chip.onclick = () => {
          selectedCat = c.name;
          selectedSubcat = null;
          renderCatChips();
          renderSubcatChips();
        };
        list.appendChild(chip);
      });
    }

    function renderSubcatChips() {
      const list = document.getElementById('subcat-chips-list');
      list.innerHTML = '';
      const presets = subcategoryPresets[selectedCat] || ['General', 'Other'];
      presets.forEach(sub => {
        const chip = document.createElement('button');
        chip.type = 'button';
        chip.className = 'subcat-chip ' + (selectedSubcat === sub ? 'active' : '');
        chip.textContent = sub;
        chip.onclick = () => {
          selectedSubcat = (selectedSubcat === sub) ? null : sub;
          renderSubcatChips();
        };
        list.appendChild(chip);
      });
    }

    function setPM(pm) {
      selectedPM = pm;
      document.querySelectorAll('#pm-card, #pm-cash, #pm-transfer').forEach(b => b.classList.remove('active'));
      const active = document.getElementById('pm-' + pm);
      if (active) active.classList.add('active');
    }

    function submitTransaction() {
      const amtVal = parseFloat(document.getElementById('inp-amount').value);
      if (isNaN(amtVal) || amtVal <= 0) {
        alert('Please enter a valid amount greater than zero.');
        return;
      }
      const cents = Math.round(amtVal * 100);
      const date = document.getElementById('inp-date').value || '2026-10-08';
      const note = document.getElementById('inp-note').value.trim();

      const newTx = {
        id: Date.now(),
        amountCents: cents,
        type: currentType,
        category: selectedCat,
        subcategory: selectedSubcat,
        date: date,
        note: note,
        paymentMethod: selectedPM,
        isDeleted: false
      };
      state.transactions.unshift(newTx);
      saveState();
      closeModal('add-modal');
      renderScreen();
      showSnackbar('Transaction saved with subcategory!');
    }

    function openTxDetail(id) {
      const tx = state.transactions.find(t => t.id === id);
      if (!tx) return;
      const body = document.getElementById('detail-modal-body');
      body.innerHTML = \`
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:14px;">
          <h3 style="font-size:18px; font-weight:800;">\${tx.category}</h3>
          <button class="icon-btn" onclick="closeModal('detail-modal')"><span class="material-symbols-rounded">close</span></button>
        </div>
        <div style="font-size:28px; font-weight:800; color:\${tx.type === 'expense' ? 'var(--expense)' : 'var(--income)'}">
          \${tx.type === 'expense' ? '-' : '+'}\${formatCents(tx.amountCents)}
        </div>
        <div style="margin-top:12px; font-size:13px; color:var(--text-muted); line-height:1.8;">
          <div><strong>Subcategory:</strong> \${tx.subcategory || 'None'}</div>
          <div><strong>Date:</strong> \${tx.date}</div>
          <div><strong>Payment:</strong> \${tx.paymentMethod || 'None'}</div>
          \${tx.note ? \`<div><strong>Note:</strong> \${tx.note}</div>\` : ''}
        </div>
        <div style="display:flex; gap:10px; margin-top:20px;">
          <button style="flex:1; padding:12px; border-radius:12px; border:1px solid var(--expense); background:none; color:var(--expense); font-weight:700; cursor:pointer;" onclick="deleteTx(\${tx.id})">Delete</button>
          <button style="flex:1; padding:12px; border-radius:12px; border:none; background:var(--card-subtle); color:var(--text); font-weight:700; cursor:pointer;" onclick="closeModal('detail-modal')">Close</button>
        </div>
      \`;
      document.getElementById('detail-modal').style.display = 'flex';
    }

    function deleteTx(id) {
      const idx = state.transactions.findIndex(t => t.id === id);
      if (idx !== -1) {
        lastDeletedTx = state.transactions[idx];
        state.transactions[idx].isDeleted = true;
        saveState();
        closeModal('detail-modal');
        renderScreen();
        showSnackbar('Transaction deleted');
      }
    }

    function undoDelete() {
      if (lastDeletedTx) {
        lastDeletedTx.isDeleted = false;
        saveState();
        lastDeletedTx = null;
        renderScreen();
        document.getElementById('snackbar').style.display = 'none';
      }
    }

    function showSnackbar(msg) {
      const el = document.getElementById('snackbar');
      document.getElementById('snackbar-msg').textContent = msg;
      el.style.display = 'flex';
      setTimeout(() => { el.style.display = 'none'; }, 4000);
    }

    function openDayDetail(dateStr) {
      const dayTxs = state.transactions.filter(t => !t.isDeleted && t.date === dateStr);
      const spent = dayTxs.filter(t => t.type === 'expense').reduce((a, b) => a + b.amountCents, 0);
      const income = dayTxs.filter(t => t.type === 'income').reduce((a, b) => a + b.amountCents, 0);

      const body = document.getElementById('detail-modal-body');
      let txsHtml = '';
      if (dayTxs.length === 0) {
        txsHtml = '<div style="color:var(--text-muted); font-size:13px; text-align:center; padding:16px;">No transactions on this date.</div>';
      } else {
        dayTxs.forEach(t => {
          txsHtml += \`
            <div class="tx-item" onclick="closeModal('detail-modal'); openTxDetail(\${t.id});">
              <div class="tx-info">
                <div class="tx-cat">\${t.category} \${t.subcategory ? '• ' + t.subcategory : ''}</div>
                <div class="tx-sub">\${t.note || ''}</div>
              </div>
              <div class="tx-amount \${t.type}">\${t.type === 'expense' ? '-' : '+'}\${formatCents(t.amountCents)}</div>
            </div>
          \`;
        });
      }

      body.innerHTML = \`
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:14px;">
          <h3 style="font-size:18px; font-weight:800;">\${dateStr}</h3>
          <button class="icon-btn" onclick="closeModal('detail-modal')"><span class="material-symbols-rounded">close</span></button>
        </div>
        <div style="display:grid; grid-template-columns:1fr 1fr; gap:10px; margin-bottom:16px;">
          <div class="card" style="margin:0; padding:12px; text-align:center;">
            <div style="font-size:11px; color:var(--text-muted);">Spent</div>
            <div style="font-size:16px; font-weight:800; color:var(--expense)">\${formatCents(spent)}</div>
          </div>
          <div class="card" style="margin:0; padding:12px; text-align:center;">
            <div style="font-size:11px; color:var(--text-muted);">Income</div>
            <div style="font-size:16px; font-weight:800; color:var(--income)">\${formatCents(income)}</div>
          </div>
        </div>
        <button class="btn-submit" style="margin-bottom:14px;" onclick="closeModal('detail-modal'); openAddModal('expense'); document.getElementById('inp-date').value = '\${dateStr}';">Add for this day</button>
        <div style="font-size:13px; font-weight:700; margin-bottom:8px;">Transactions (\${dayTxs.length})</div>
        \${txsHtml}
      \`;
      document.getElementById('detail-modal').style.display = 'flex';
    }

    function openWriteNoteModal() {
      const body = document.getElementById('detail-modal-body');
      body.innerHTML = \`
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:14px;">
          <h3 style="font-size:18px; font-weight:800;">Write Financial Note</h3>
          <button class="icon-btn" onclick="closeModal('detail-modal')"><span class="material-symbols-rounded">close</span></button>
        </div>
        <div class="form-group">
          <label class="form-label">TITLE</label>
          <input type="text" id="j-title" class="form-input" placeholder="Reflection topic..." />
        </div>
        <div class="form-group">
          <label class="form-label">REFLECTION (MAX 2000 CHARACTERS)</label>
          <textarea id="j-body" class="form-input" rows="4" placeholder="How do you feel about your spending today?"></textarea>
        </div>
        <div class="form-group">
          <label class="form-label">MOOD</label>
          <select id="j-mood" class="form-input">
            <option value="proud">Proud</option>
            <option value="good">Good</option>
            <option value="neutral">Neutral</option>
            <option value="stressed">Stressed</option>
          </select>
        </div>
        <button class="btn-submit" onclick="saveNote()">Save Note</button>
      \`;
      document.getElementById('detail-modal').style.display = 'flex';
    }

    function saveNote() {
      const title = document.getElementById('j-title').value.trim();
      const body = document.getElementById('j-body').value.trim();
      const mood = document.getElementById('j-mood').value;
      if (!body) { alert('Note body is required'); return; }
      state.journal.unshift({
        id: Date.now(),
        title: title || 'Financial Note',
        body: body,
        mood: mood,
        date: '2026-10-08',
        isDeleted: false
      });
      saveState();
      closeModal('detail-modal');
      switchTab('notes');
      showSnackbar('Reflection saved!');
    }

    function recordBill(id) {
      const bill = state.recurring.find(b => b.id === id);
      if (!bill) return;
      state.transactions.unshift({
        id: Date.now(),
        amountCents: bill.amountCents,
        type: bill.type,
        category: bill.category,
        date: bill.nextDueDate,
        note: 'Recurring: ' + bill.name,
        paymentMethod: 'card',
        isDeleted: false
      });
      saveState();
      renderScreen();
      showSnackbar('Recorded payment for ' + bill.name);
    }

    function openReports() {
      alert('Reports & Analytics: Total Monthly Spent: ' + formatCents(state.transactions.filter(t => !t.isDeleted && t.type === 'expense').reduce((a, b) => a + b.amountCents, 0)));
    }

    function openSavings() {
      alert('Savings Goals: Emergency Fund target ' + formatCents(500000) + ' (' + Math.round((150000/500000)*100) + '% complete)');
    }

    function openSettings() {
      const body = document.getElementById('detail-modal-body');
      body.innerHTML = \`
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:14px;">
          <h3 style="font-size:18px; font-weight:800;">Settings</h3>
          <button class="icon-btn" onclick="closeModal('detail-modal')"><span class="material-symbols-rounded">close</span></button>
        </div>
        <div style="font-size:13px; line-height:1.8;">
          <div><strong>Currency:</strong> \${state.currency} (Default)</div>
          <div><strong>Start of Week:</strong> \${state.startOfWeek}</div>
          <div><strong>Architecture:</strong> Local-first SQLite (no server required)</div>
          <div style="margin-top:14px;">
            <button class="btn-submit" style="background:#EF4444;" onclick="if(confirm('Wipe all local data?')){localStorage.clear();location.reload();}">Clear All Local Data</button>
          </div>
        </div>
      \`;
      document.getElementById('detail-modal').style.display = 'flex';
    }

    renderScreen();
  </script>
</body>
</html>`;

const server = http.createServer((req, res) => {
  res.writeHead(200, {
    'Content-Type': 'text/html; charset=utf-8',
    'Cache-Control': 'no-cache'
  });
  res.end(htmlContent);
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`BudgetFlow live server listening on port ${PORT}`);
});
