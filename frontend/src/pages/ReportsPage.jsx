import { useEffect, useState } from 'react';
import api from '../services/api';

const currency = (value) =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(Number(value || 0));

export default function ReportsPage() {
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const data = await api.getReports();
        setReport(data);
      } catch (error) {
        console.error(error);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  if (loading) return <div className="page-loading">Loading reports...</div>;
  if (!report) return <div className="page-loading">Unable to load reports.</div>;

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <div className="eyebrow">Reports</div>
          <h1>Financial insights</h1>
        </div>
      </div>

      <div className="metric-grid compact">
        <div className="metric-card accent-green"><span>Total income</span><strong>{currency(report.totalIncome)}</strong></div>
        <div className="metric-card accent-red"><span>Total expenses</span><strong>{currency(report.totalExpenses)}</strong></div>
        <div className="metric-card accent-purple"><span>Largest spend</span><strong>{report.largestSpendingCategory?.category || 'No data'}</strong></div>
      </div>

      <div className="panel-grid two-up">
        <section className="panel-card">
          <div className="panel-header"><h3>Monthly trend</h3></div>
          <div className="list-stack">
            {(report.monthlyTrend || []).map((entry) => (
              <div key={entry.month} className="budget-row">
                <div className="budget-row-top">
                  <strong>{entry.month}</strong>
                  <span>{currency(entry.income)} income</span>
                </div>
                <div className="budget-row-bottom">
                  <small>{currency(entry.expenses)} spent</small>
                  <small>{currency(Number(entry.income) - Number(entry.expenses))} net</small>
                </div>
              </div>
            ))}
          </div>
        </section>

        <section className="panel-card">
          <div className="panel-header"><h3>Category breakdown</h3></div>
          <div className="list-stack">
            {(report.categoryBreakdown || []).map((entry) => (
              <div key={entry.category} className="transaction-row">
                <strong>{entry.category}</strong>
                <span className="amount expense">{currency(entry.amount)}</span>
              </div>
            ))}
          </div>
        </section>
      </div>
    </div>
  );
}
