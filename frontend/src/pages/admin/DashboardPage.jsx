import { useEffect, useMemo, useState } from "react";
import { useApiFetch } from "../../api/useApiFetch";

import "./dashboard.css";


const KPIS_ENDPOINT = "/api/dashboard/kpis";

const RANKING_TABS = [
  { key: "top10View", label: "Visualizaciones" },
  { key: "top10Favoritos", label: "Favoritos" },
  { key: "top10Comentarios", label: "Comentarios" },
  { key: "top10Valoraciones", label: "Valoraciones" },
];

const EVOLUTION_TABS = [
  { key: "evolucionVisualizaciones", label: "Visualizaciones", valueKey: "visualizaciones" },
  { key: "evolucionFavoritos", label: "Favoritos", valueKey: "favoritos" },
  { key: "evolucionComentarios", label: "Comentarios", valueKey: "comentarios" },
  { key: "evolucionValoraciones", label: "Valoraciones", valueKey: "valoraciones" },
];

/* -------------------------------------------------------------------- */
/* Hook de datos                                                        */
/* -------------------------------------------------------------------- */

function useDashboardData() {
  const [state, setState] = useState({ status: "loading", data: null, error: null });

  const apiFetch = useApiFetch();

  useEffect(() => {
    let cancelled = false;

    async function fetchData() {
      try {
        const response = await apiFetch(KPIS_ENDPOINT);
        if (!response.ok) {
          throw new Error(`Error ${response.status} al consultar el dashboard`);
        }
        const json = await response.json();
        if (!cancelled) setState({ status: "ready", data: json, error: null });
      } catch (error) {
        if (!cancelled) setState({ status: "error", data: null, error });
      }
    }

    fetchData();
    return () => {
      cancelled = true;
    };
  }, []);

  return state;
}

/* -------------------------------------------------------------------- */
/* KPIs                                                                  */
/* -------------------------------------------------------------------- */

function KpiCards({ data }) {
  const items = [
    { label: "Figuras publicadas", value: data.totalFigures },
    { label: "Visualizaciones", value: data.totalViews },
    { label: "Favoritos", value: data.totalFavorites },
    { label: "Comentarios", value: data.totalComments },
    { label: "Valoración media", value: data.averageRating.toFixed(2) },
  ];

  return (
    <div className="dashboard-kpis">
      {items.map((item) => (
        <div className="dashboard-panel dashboard-kpi" key={item.label}>
          <span className="dashboard-kpi__value">{item.value}</span>
          <span className="dashboard-kpi__label">{item.label}</span>
        </div>
      ))}
    </div>
  );
}

/* -------------------------------------------------------------------- */
/* Ranking (tabla reutilizable)                                         */
/* -------------------------------------------------------------------- */

function RankingTable({ data }) {
  if (!data || data.length === 0) {
    return <p className="dashboard-empty">Todavía no hay datos suficientes para este ranking.</p>;
  }

  return (
    <div className="dashboard-table-wrap">
      <table className="dashboard-table">
        <thead>
          <tr>
            <th>Figura</th>
            <th>Visualizaciones</th>
            <th>Favoritos</th>
            <th>Comentarios</th>
            <th>Valoración</th>
          </tr>
        </thead>
        <tbody>
          {data.map((row, index) => (
            <tr key={row.figura + index}>
              <td>
                <span className="dashboard-table__rank">{index + 1}</span>
                {row.figura}
              </td>
              <td>{row.visualizaciones}</td>
              <td>{row.favoritos}</td>
              <td>{row.comentarios}</td>
              <td>{row.valoracionMedia > 0 ? row.valoracionMedia.toFixed(2) : "—"}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function RankingsSection({ data }) {
  const [activeTab, setActiveTab] = useState(RANKING_TABS[0].key);

  return (
    <section className="dashboard-panel dashboard-section">
      <h2 className="dashboard-section__title">Rankings</h2>
      <div className="catalog-filter dashboard-tabs">
        {RANKING_TABS.map((tab) => (
          <button
            key={tab.key}
            type="button"
            className={
              "catalog-filter__item" +
              (activeTab === tab.key ? " catalog-filter__item--active" : "")
            }
            onClick={() => setActiveTab(tab.key)}
          >
            {tab.label}
          </button>
        ))}
      </div>
      <RankingTable data={data[activeTab]} />
      {activeTab === "top10Valoraciones" && (
        <p className="dashboard-note">
          Este ranking exige un mínimo de 3 valoraciones por figura para evitar resultados
          estadísticamente engañosos.
        </p>
      )}
    </section>
  );
}

/* -------------------------------------------------------------------- */
/* Tendencias                                                            */
/* -------------------------------------------------------------------- */

function TrendingSection({ trendingFigures }) {
  const maxViews = Math.max(...trendingFigures.map((item) => item.visualizaciones), 1);

  return (
    <section className="dashboard-panel dashboard-section">
      <h2 className="dashboard-section__title">🔥 Popularidad reciente de las figuras (30 días)</h2>
      <ul className="dashboard-trending">
        {trendingFigures.map((item, index) => (
          <li className="dashboard-trending__item" key={item.figura + index}>
            <span className="dashboard-trending__name">
              <span className="dashboard-table__rank">{index + 1}</span>
              {item.figura}
            </span>
            <div className="dashboard-trending__bar-track">
              <div
                className="dashboard-trending__bar-fill"
                style={{ width: `${(item.visualizaciones / maxViews) * 100}%` }}
              />
            </div>
            <span className="dashboard-trending__value">{item.visualizaciones}</span>
          </li>
        ))}
      </ul>
    </section>
  );
}

/* -------------------------------------------------------------------- */
/* Evolución temporal                                                    */
/* -------------------------------------------------------------------- */

function formatPeriodo(periodo) {
  const [year, month] = periodo.split("-");
  const meses = [
    "ene", "feb", "mar", "abr", "may", "jun",
    "jul", "ago", "sep", "oct", "nov", "dic",
  ];
  const monthLabel = meses[Number(month) - 1] ?? month;
  return `${monthLabel} ${year}`;
}

function EvolutionChart({ series, valueKey }) {
  if (!series || series.length === 0) {
    return <p className="dashboard-empty">Todavía no hay datos suficientes para esta serie.</p>;
  }

  const maxValue = Math.max(...series.map((point) => point[valueKey]), 1);
  const chartHeight = 160;

  return (
    <div className="dashboard-chart">
      {series.map((point) => {
        const value = point[valueKey];
        const barHeight = Math.max((value / maxValue) * chartHeight, 4);
        return (
          <div className="dashboard-chart__column" key={point.periodo}>
            <span className="dashboard-chart__value">{value}</span>
            <div
              className="dashboard-chart__bar"
              style={{ height: `${barHeight}px` }}
            />
            <span className="dashboard-chart__label">{formatPeriodo(point.periodo)}</span>
          </div>
        );
      })}
    </div>
  );
}

function EvolutionSection({ data }) {
  const [activeTab, setActiveTab] = useState(EVOLUTION_TABS[0].key);
  const activeConfig = useMemo(
    () => EVOLUTION_TABS.find((tab) => tab.key === activeTab),
    [activeTab]
  );

  return (
    <section className="dashboard-panel dashboard-section">
      <h2 className="dashboard-section__title">Evolución temporal</h2>
      <div className="catalog-filter dashboard-tabs">
        {EVOLUTION_TABS.map((tab) => (
          <button
            key={tab.key}
            type="button"
            className={
              "catalog-filter__item" +
              (activeTab === tab.key ? " catalog-filter__item--active" : "")
            }
            onClick={() => setActiveTab(tab.key)}
          >
            {tab.label}
          </button>
        ))}
      </div>
      <EvolutionChart series={data[activeConfig.key]} valueKey={activeConfig.valueKey} />
    </section>
  );
}

/* -------------------------------------------------------------------- */
/* Página                                                                 */
/* -------------------------------------------------------------------- */

export default function DashboardPage() {
  const { status, data, error } = useDashboardData();

  if (status === "loading") {
    return (
      <main className="dashboard-page">
        <p className="catalog-status">Cargando estadísticas…</p>
      </main>
    );
  }

  if (status === "error") {
    return (
      <main className="dashboard-page">
        <p className="catalog-status catalog-status--error">
          No se pudo cargar el dashboard{error?.message ? `: ${error.message}` : "."}
        </p>
      </main>
    );
  }

  return (
    <main className="dashboard-page">
      <header className="dashboard-header">
        <h1 className="dashboard-header__title">Dashboard de Estadísticas</h1>
      </header>

      <KpiCards data={data} />
      <RankingsSection data={data} />
      <TrendingSection trendingFigures={data.trendingFigures} />
      <EvolutionSection data={data} />
    </main>
  );
}
