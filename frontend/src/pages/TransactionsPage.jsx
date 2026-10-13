import { useEffect, useMemo, useState } from 'react';
import api from '../services/api';

const currency = (value) =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(Number(value || 0));

const emptyForm = {
  title: '',
  amount: '',
  type: 'EXPENSE',
  category: 'Food',
  date: new Date().toISOString().slice(0, 10),
  paymentMethod: 'Card',
  notes: '',
};

export default function TransactionsPage() {
  const [transactions, setTransactions] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadData = async () => {
    setLoading(true);
    try {
      const [txData, categoryData] = await Promise.all([
        api.getTransactions(),
        api.getCategories(),
      ]);
      setTransactions(txData);
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

  const summary = useMemo(() => {
    const income = transactions.filter((t) => t.type === 'INCOME').reduce((sum, item) => sum + Number(item.amount), 0);
    const expense = transactions.filter((t) => t.type === 'EXPENSE').reduce((sum, item) => sum + Number(item.amount), 0);
    return { income, expense, net: income - expense };
  }, [transactions]);

  const onChange = (event) => {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const submit = async (event) => {
    event.preventDefault();
    try {
      await api.createTransaction({ ...form, amount: Number(form.amount) });
      setForm(emptyForm);
      await loadData();
    } catch (err) {
      setError(err.message);
    }
  };

  const remove = async (id) => {
    try {
      await api.deleteTransaction(id);
      await loadData();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <div className="eyebrow">Transactions</div>
          <h1>Cash flow</h1>
        </div>
      </div>

      <div className="metric-grid compact">
        <div className="metric-card accent-green"><span>Income</span><strong>{currency(summary.income)}</strong></div>
        <div className="metric-card accent-red"><span>Expenses</span><strong>{currency(summary.expense)}</strong></div>
        <div className="metric-card accent-blue"><span>Net</span><strong>{currency(summary.net)}</strong></div>
      </div>

      <div className="panel-grid two-up">
        <section className="panel-card">
          <div className="panel-header">
            <h3>Add transaction</h3>
          </div>
          <form className="stack-form" onSubmit={submit}>
            <div className="two-col">
              <label>
                Title
                <input name="title" value={form.title} onChange={onChange} required />
              </label>
              <label>
                Amount
                <input name="amount" type="number" min="0.01" step="0.01" value={form.amount} onChange={onChange} required />
              </label>
            </div>

            <div className="two-col">
              <label>
                Type
                <select name="type" value={form.type} onChange={onChange}>
                  <option value="INCOME">Income</option>
                  <option value="EXPENSE">Expense</option>
                </select>
              </label>
              <label>
                Category
                <select name="category" value={form.category} onChange={onChange}>
                  {(categories.length ? categories : [{ name: 'Food' }, { name: 'Shopping' }, { name: 'Bills' }, { name: 'Housing' }]).map((c) => (
                    <option key={c.name} value={c.name}>{c.name}</option>
                  ))}
                </select>
              </label>
            </div>

            <div className="two-col">
              <label>
                Date
                <input name="date" type="date" value={form.date} onChange={onChange} required />
              </label>
              <label>
                Payment method
                <input name="paymentMethod" value={form.paymentMethod} onChange={onChange} required />
              </label>
            </div>

            <label>
              Notes
              <textarea name="notes" value={form.notes} onChange={onChange} rows="3" />
            </label>

            {error && <div className="error-box">{error}</div>}
            <button type="submit">Save transaction</button>
          </form>
        </section>

        <section className="panel-card">
          <div className="panel-header">
            <h3>Recent entries</h3>
          </div>
          <div className="list-stack">
            {loading ? <div className="inline-loading">Loading...</div> : transactions.map((tx) => (
              <div className="transaction-row" key={tx.id}>
                <div>
                  <strong>{tx.title}</strong>
                  <small>{tx.date} · {tx.category}</small>
                </div>
                <div className="transaction-actions">
                  <span className={`amount ${tx.type === 'INCOME' ? 'income' : 'expense'}`}>
                    {tx.type === 'INCOME' ? '+' : '-'}{currency(tx.amount)}
                  </span>
                  <button className="small-button" onClick={() => remove(tx.id)}>Delete</button>
                </div>
              </div>
            ))}
          </div>
        </section>
      </div>
    </div>
  );
}
