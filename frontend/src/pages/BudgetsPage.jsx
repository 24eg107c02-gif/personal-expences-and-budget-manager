import { useEffect, useState } from 'react';
import api from '../services/api';

const currency = (value) =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(Number(value || 0));

const getMonthValue = () => new Date().toISOString().slice(0, 7);

export default function BudgetsPage() {
  const [budgets, setBudgets] = useState([]);
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState({ category: 'Food', monthlyLimit: '800', month: getMonthValue() });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadData = async () => {
    try {
      const [budgetData, categoryData] = await Promise.all([api.getBudgets(), api.getCategories()]);
      setBudgets(budgetData);
      setCategories(categoryData.filter((c) => c.type === 'EXPENSE'));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const submit = async (event) => {
    event.preventDefault();
    try {
      await api.createBudget({ ...form, monthlyLimit: Number(form.monthlyLimit) });
      setForm((prev) => ({ ...prev, category: prev.category, monthlyLimit: '', month: prev.month }));
      await loadData();
    } catch (err) {
      setError(err.message);
    }
  };

  const remove = async (id) => {
    try {
      await api.deleteBudget(id);
      await loadData();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <div className="eyebrow">Budgets</div>
          <h1>Monthly targets</h1>
        </div>
      </div>

      <div className="panel-grid two-up">
        <section className="panel-card">
          <div className="panel-header"><h3>Create budget</h3></div>
          <form className="stack-form" onSubmit={submit}>
            <div className="two-col">
              <label>
                Category
                <select name="category" value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })}>
                  {(categories.length ? categories : [{ name: 'Food' }, { name: 'Housing' }, { name: 'Shopping' }, { name: 'Bills' }]).map((c) => (
                    <option key={c.name} value={c.name}>{c.name}</option>
                  ))}
                </select>
              </label>
              <label>
                Month
                <input type="month" value={form.month} onChange={(e) => setForm({ ...form, month: e.target.value })} required />
              </label>
            </div>

            <label>
              Monthly limit
              <input type="number" min="0.01" step="0.01" value={form.monthlyLimit} onChange={(e) => setForm({ ...form, monthlyLimit: e.target.value })} required />
            </label>

            {error && <div className="error-box">{error}</div>}
            <button type="submit">Save budget</button>
          </form>
        </section>

        <section className="panel-card">
          <div className="panel-header"><h3>Budget performance</h3></div>
          <div className="list-stack">
            {loading ? <div className="inline-loading">Loading...</div> : budgets.map((budget) => (
              <div key={budget.id} className="budget-row">
                <div className="budget-row-top">
                  <strong>{budget.category}</strong>
                  <span>{currency(budget.spentAmount)} / {currency(budget.monthlyLimit)}</span>
                </div>
                <div className="progress-bar">
                  <span style={{ width: `${Math.min(100, Math.max(0, budget.percentageUsed))}%` }} />
                </div>
                <div className="budget-row-bottom">
                  <small>{budget.month}</small>
                  <small>{budget.remainingAmount < 0 ? 'Over budget' : 'On track'} · {currency(budget.remainingAmount)} left</small>
                  <button className="small-button" onClick={() => remove(budget.id)}>Delete</button>
                </div>
              </div>
            ))}
          </div>
        </section>
      </div>
    </div>
  );
}
