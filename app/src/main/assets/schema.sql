-- =====================================================================
-- BudgetFlow SQLite Database Schema
-- Architecture: Local-first, offline-capable, zero server requirement
-- Rule: All monetary amounts strictly stored as INTEGER CENTS
-- =====================================================================

-- 1. CATEGORIES TABLE
CREATE TABLE IF NOT EXISTS categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name TEXT NOT NULL,
    type TEXT NOT NULL DEFAULT 'both', -- 'expense', 'income', or 'both'
    iconName TEXT NOT NULL DEFAULT 'category',
    colorHex TEXT NOT NULL DEFAULT '#10B981',
    isPreset INTEGER NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX IF NOT EXISTS index_categories_name ON categories (name);

-- 2. TRANSACTIONS TABLE
CREATE TABLE IF NOT EXISTS transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    amountCents INTEGER NOT NULL,          -- Positive integer cents (e.g., $15.50 = 1550)
    type TEXT NOT NULL,                   -- 'expense' or 'income' (user selected)
    category TEXT NOT NULL,
    subcategory TEXT,                     -- Subcategory (e.g. 'Groceries', 'Fuel', 'Rent')
    dateEpochDay INTEGER NOT NULL,        -- LocalDate.toEpochDay()
    note TEXT,                            -- Max 500 characters
    paymentMethod TEXT,                   -- 'cash', 'card', 'transfer', or NULL
    isRecurring INTEGER NOT NULL DEFAULT 0,
    recurringRuleId INTEGER,
    isDeleted INTEGER NOT NULL DEFAULT 0, -- Soft delete flag (0 = active, 1 = deleted)
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS index_transactions_dateEpochDay ON transactions (dateEpochDay);
CREATE INDEX IF NOT EXISTS index_transactions_category ON transactions (category);
CREATE INDEX IF NOT EXISTS index_transactions_type ON transactions (type);
CREATE INDEX IF NOT EXISTS index_transactions_isDeleted ON transactions (isDeleted);

-- 3. BUDGETS TABLE
CREATE TABLE IF NOT EXISTS budgets (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    category TEXT NOT NULL,               -- Category name or 'OVERALL'
    monthYear TEXT NOT NULL,              -- Format: 'YYYY-MM' (e.g., '2026-10')
    limitCents INTEGER NOT NULL           -- Monthly limit in integer cents
);
CREATE UNIQUE INDEX IF NOT EXISTS index_budgets_category_monthYear ON budgets (category, monthYear);

-- 4. RECURRING TRANSACTIONS TABLE
CREATE TABLE IF NOT EXISTS recurring_transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name TEXT NOT NULL,
    amountCents INTEGER NOT NULL,          -- Integer cents
    type TEXT NOT NULL,                   -- 'expense' or 'income'
    category TEXT NOT NULL,
    frequency TEXT NOT NULL,              -- 'weekly', 'monthly', 'yearly'
    nextDueDateEpochDay INTEGER NOT NULL,
    dayOfMonth INTEGER NOT NULL DEFAULT 1,-- Anchor day for month-end clamping (e.g. 31)
    endDateEpochDay INTEGER,
    isPaused INTEGER NOT NULL DEFAULT 0,
    isDeleted INTEGER NOT NULL DEFAULT 0,
    note TEXT,
    createdAt INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS index_recurring_nextDueDate ON recurring_transactions (nextDueDateEpochDay);
CREATE INDEX IF NOT EXISTS index_recurring_isPaused ON recurring_transactions (isPaused);
CREATE INDEX IF NOT EXISTS index_recurring_isDeleted ON recurring_transactions (isDeleted);

-- 5. JOURNAL & FINANCIAL NOTES TABLE
CREATE TABLE IF NOT EXISTS journal_entries (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    title TEXT,
    body TEXT NOT NULL,                   -- Max 2000 characters
    dateEpochDay INTEGER NOT NULL,
    linkedTransactionId INTEGER,          -- Optional link to transaction
    mood TEXT,                            -- 'good', 'neutral', 'stressed', 'proud'
    tags TEXT,                            -- Comma-separated tags
    isPinned INTEGER NOT NULL DEFAULT 0,  -- 1 if pinned to top
    isDeleted INTEGER NOT NULL DEFAULT 0, -- Soft delete flag
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS index_journal_dateEpochDay ON journal_entries (dateEpochDay);
CREATE INDEX IF NOT EXISTS index_journal_isPinned ON journal_entries (isPinned);
CREATE INDEX IF NOT EXISTS index_journal_isDeleted ON journal_entries (isDeleted);

-- 6. SAVINGS GOALS TABLE
CREATE TABLE IF NOT EXISTS savings_goals (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name TEXT NOT NULL,
    targetAmountCents INTEGER NOT NULL,   -- Target amount in integer cents
    targetDateEpochDay INTEGER,
    isArchived INTEGER NOT NULL DEFAULT 0,
    createdAt INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS index_savings_goals_isArchived ON savings_goals (isArchived);

-- 7. SAVINGS CONTRIBUTIONS TABLE
CREATE TABLE IF NOT EXISTS savings_contributions (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    goalId INTEGER NOT NULL,
    amountCents INTEGER NOT NULL,         -- Contribution in integer cents
    dateEpochDay INTEGER NOT NULL,
    note TEXT,
    createdAt INTEGER NOT NULL,
    FOREIGN KEY (goalId) REFERENCES savings_goals(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS index_savings_contributions_goalId ON savings_contributions (goalId);

-- 8. USER SETTINGS TABLE (Key-Value)
CREATE TABLE IF NOT EXISTS user_settings (
    key TEXT PRIMARY KEY NOT NULL,
    value TEXT NOT NULL
);
