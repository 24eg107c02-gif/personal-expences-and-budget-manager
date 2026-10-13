import { useEffect, useState } from 'react';
import api from '../services/api';

const currency = (value) =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(Number(value || 0));

export default function DashboardPage() {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const data = await api.getDashboard();
        setSummary(data);
      } catch (error) {
        console.error(error);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  if (loading) return <div className="page-loading">Loading dashboard...</div>;
  if (!summary) return <div className="page-loading">Unable to load dashboard.</div>;

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <div className="eyebrow">Overview</div>
          <h1>Welcome back, {summary.fullName}</h1>
        </div>
      </div>

      <div className="metric-grid">
        <div className="metric-card accent-blue">
          <span>Balance</span>
          <strong>{currency(summary.balance)}</strong>
        </div>
        <div className="metric-card accent-green">
          <span>Income</span>
          <strong>{currency(summary.totalIncome)}</strong>
        </div>
        <div className="metric-card accent-red">
          <span>Expenses</span>
          <strong>{currency(summary.totalExpenses)}</strong>
        </div>
        <div className="metric-card accent-purple">
          <span>Remaining budget</span>
          <strong>{currency(summary.remainingBudget)}</strong>
        </div>
      </div>

      <div className="panel-grid two-up">
        <section className="panel-card">
          <div className="panel-header">
            <h3>Budget overview</h3>
          </div>
          <div className="list-stack">
            {(summary.budgetSummaries || []).map((budget) => (
              <div key={budget.id} className="budget-row">
                <div className="budget-row-top">
                  <strong>{budget.category}</strong>
                  <span>{currency(budget.spentAmount)} / {currency(budget.monthlyLimit)}</span>
                </div>
                <div className="progress-bar">
                  <span style={{ width: `${Math.min(100, Math.max(0, budget.percentageUsed))}%` }} />
                </div>
                <small>{budget.remainingAmount < 0 ? 'Over plan' : 'On track'} · {currency(budget.remainingAmount)} left</small>
              </div>
            ))}
          </div>
        </section>

        <section className="panel-card">
          <div className="panel-header">
            <h3>Recent transactions</h3>
          </div>
          <div className="list-stack">
            {(summary.recentTransactions || []).map((tx) => (
              <div key={tx.id} className="transaction-row">
                <div>
                  <strong>{tx.title}</strong>
                  <small>{tx.category} · {tx.paymentMethod}</small>
                </div>
                <div className={`amount ${tx.type === 'INCOME' ? 'income' : 'expense'}`}>
                  {tx.type === 'INCOME' ? '+' : '-'}{currency(tx.amount)}
                </div>
              </div>
            ))}
          </div>
        </section>
      </div>
    </div>
  );
}
