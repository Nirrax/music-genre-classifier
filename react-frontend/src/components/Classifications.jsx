import { useState, useEffect } from "react";
import useAxiosPrivate from "../hooks/useAxiosPrivate";
import { useNavigate, useLocation, Link } from "react-router-dom";
import {
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";

const COLORS = [
  "#90EE90",
  "#87CEEB",
  "#FFB6C1",
  "#FFD700",
  "#FF69B4",
  "#98FB98",
  "#DDA0DD",
  "#F0E68C",
];

const Classifications = () => {
  const [classifications, setClassifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expandedId, setExpandedId] = useState(null);

  const axiosPrivate = useAxiosPrivate();
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    let isMounted = true;
    const controller = new AbortController();

    const getClassifications = async () => {
      try {
        const response = await axiosPrivate.get("/classifications", {
          signal: controller.signal,
        });
        console.log(response.data);
        isMounted && setClassifications(response.data);
        isMounted && setLoading(false);
      } catch (err) {
        console.error(err);
        navigate("/login", { state: { from: location }, replace: true });
      }
    };

    getClassifications();

    return () => {
      isMounted = false;
      controller.abort();
    };
  }, []);

  const toggleExpanded = (id) => {
    setExpandedId(expandedId === id ? null : id);
  };

  // Improved custom label for pie chart - only show if percentage is significant
  const renderCustomLabel = ({
    cx,
    cy,
    midAngle,
    innerRadius,
    outerRadius,
    percent,
  }) => {
    // Only show label if slice is bigger than 5%
    if (percent < 0.05) return null;

    const RADIAN = Math.PI / 180;
    const radius = outerRadius + 25;
    const x = cx + radius * Math.cos(-midAngle * RADIAN);
    const y = cy + radius * Math.sin(-midAngle * RADIAN);

    return (
      <text
        x={x}
        y={y}
        fill="white"
        textAnchor={x > cx ? "start" : "end"}
        dominantBaseline="central"
        style={{ fontSize: "14px", fontWeight: "500" }}
      >
        {`${(percent * 100).toFixed(0)}%`}
      </text>
    );
  };

  if (loading) {
    return (
      <section style={{ maxWidth: "100%", minHeight: "400px" }}>
        <p>Loading classifications...</p>
      </section>
    );
  }

  if (classifications.length === 0) {
    return (
      <section style={{ maxWidth: "100%", minHeight: "400px" }}>
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            marginBottom: "2rem",
          }}
        >
          <h1 style={{ margin: 0 }}>Classifications</h1>
          <button onClick={() => navigate("/upload")}>
            Create Classification
          </button>
        </div>
        <p style={{ marginTop: "2rem", opacity: 0.8 }}>
          No classifications found.
        </p>
      </section>
    );
  }

  return (
    <section style={{ maxWidth: "100%", minHeight: "400px" }}>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: "0rem",
        }}
      >
        <h1 style={{ margin: 0 }}>Classifications</h1>
        <button onClick={() => navigate("/upload")}>
          Create Classification
        </button>
      </div>

      <div style={{ marginTop: "1rem" }}>
        {classifications.map((classification) => {
          // Prepare data for genre count pie chart
          const genreCountData = classification.genreCount
            ? Object.entries(classification.genreCount).map(
                ([genre, count]) => ({
                  name: genre,
                  value: count,
                }),
              )
            : [];

          // Prepare data for genre sequence chart (shows progression)
          const genreSequenceData = classification.genreSequence
            ? classification.genreSequence.map((genre, index) => ({
                position: index + 1,
                genre,
                value: 1, // Each position has a value of 1 for the bar height
              }))
            : [];

          // Create a map of genre to color
          const genreToColor = {};
          if (classification.genreCount) {
            Object.keys(classification.genreCount).forEach((genre, index) => {
              genreToColor[genre] = COLORS[index % COLORS.length];
            });
          }

          return (
            <div
              key={classification.id}
              style={{
                marginBottom: "1.5rem",
                padding: "1rem",
                background: "rgba(255, 255, 255, 0.1)",
                borderRadius: "0.5rem",
              }}
            >
              {/* Horizontal layout for main info */}
              <div
                style={{
                  display: "grid",
                  gridTemplateColumns: "2fr 1fr 1fr auto",
                  gap: "1rem",
                  alignItems: "center",
                  marginBottom: expandedId === classification.id ? "1rem" : "0",
                }}
              >
                <div>
                  <label
                    style={{
                      fontSize: "0.75rem",
                      opacity: 0.7,
                      display: "block",
                    }}
                  >
                    Filename
                  </label>
                  <p
                    style={{
                      fontSize: "1rem",
                      fontWeight: "bold",
                      marginTop: "0.25rem",
                      wordBreak: "break-word",
                    }}
                  >
                    {classification.filename}
                  </p>
                </div>

                <div>
                  <label
                    style={{
                      fontSize: "0.75rem",
                      opacity: 0.7,
                      display: "block",
                    }}
                  >
                    Genre
                  </label>
                  <p
                    style={{
                      fontSize: "0.9rem",
                      marginTop: "0.25rem",
                      color: "#90EE90",
                    }}
                  >
                    {classification.genre}
                  </p>
                </div>

                <div>
                  <label
                    style={{
                      fontSize: "0.75rem",
                      opacity: 0.7,
                      display: "block",
                    }}
                  >
                    Completed
                  </label>
                  <p
                    style={{
                      fontSize: "0.85rem",
                      marginTop: "0.25rem",
                    }}
                  >
                    {classification.completedAt &&
                      new Date(classification.completedAt).toLocaleDateString(
                        "en-US",
                        {
                          month: "short",
                          day: "numeric",
                          year: "numeric",
                        },
                      )}
                  </p>
                </div>

                <button
                  onClick={() => toggleExpanded(classification.id)}
                  style={{
                    padding: "0.5rem 1rem",
                    whiteSpace: "nowrap",
                  }}
                >
                  {expandedId === classification.id ? "Show Less" : "Show More"}
                </button>
              </div>

              {expandedId === classification.id && (
                <div
                  style={{
                    marginTop: "1rem",
                    paddingTop: "1rem",
                    borderTop: "1px solid rgba(255, 255, 255, 0.2)",
                  }}
                >
                  <div
                    style={{
                      display: "grid",
                      gridTemplateColumns: "repeat(2, 1fr)",
                      gap: "1rem",
                      marginBottom: "1.5rem",
                    }}
                  >
                    <div>
                      <label
                        style={{
                          fontSize: "0.75rem",
                          opacity: 0.7,
                          display: "block",
                        }}
                      >
                        ID
                      </label>
                      <p
                        style={{
                          fontSize: "0.7rem",
                          marginTop: "0.25rem",
                          wordBreak: "break-all",
                        }}
                      >
                        {classification.id}
                      </p>
                    </div>

                    <div>
                      <label
                        style={{
                          fontSize: "0.75rem",
                          opacity: 0.7,
                          display: "block",
                        }}
                      >
                        Issued At
                      </label>
                      <p
                        style={{
                          fontSize: "0.85rem",
                          marginTop: "0.25rem",
                        }}
                      >
                        {classification.issuedAt &&
                          new Date(classification.issuedAt).toLocaleDateString(
                            "en-US",
                            {
                              year: "numeric",
                              month: "long",
                              day: "numeric",
                            },
                          )}
                      </p>
                    </div>
                  </div>

                  {/* Charts Section */}
                  <div
                    style={{
                      display: "grid",
                      gridTemplateColumns: "repeat(2, 1fr)",
                      gap: "2rem",
                      marginTop: "2rem",
                    }}
                  >
                    {/* Genre Count Pie Chart */}
                    <div>
                      <label
                        style={{
                          fontSize: "0.9rem",
                          opacity: 0.9,
                          display: "block",
                          marginBottom: "1rem",
                          fontWeight: "bold",
                        }}
                      >
                        Genre Distribution
                      </label>
                      <ResponsiveContainer width="100%" height={350}>
                        <PieChart>
                          <Pie
                            data={genreCountData}
                            cx="50%"
                            cy="50%"
                            labelLine={true}
                            label={renderCustomLabel}
                            outerRadius={90}
                            innerRadius={30}
                            fill="#8884d8"
                            dataKey="value"
                            paddingAngle={2}
                            stroke="rgba(0,0,0,0.3)"
                            strokeWidth={2}
                          >
                            {genreCountData.map((entry, index) => (
                              <Cell
                                key={`cell-${index}`}
                                fill={COLORS[index % COLORS.length]}
                              />
                            ))}
                          </Pie>
                          <Tooltip
                            contentStyle={{
                              backgroundColor: "rgba(0,0,0,0.9)",
                              border: "1px solid rgba(255,255,255,0.3)",
                              borderRadius: "0.5rem",
                              color: "#fff",
                              padding: "10px",
                            }}
                            itemStyle={{ color: "#fff" }}
                          />
                          <Legend
                            wrapperStyle={{
                              color: "#fff",
                              paddingTop: "20px",
                            }}
                            iconType="circle"
                            formatter={(value) => (
                              <span style={{ color: "#fff", fontSize: "13px" }}>
                                {value}
                              </span>
                            )}
                          />
                        </PieChart>
                      </ResponsiveContainer>
                    </div>

                    {/* Genre Sequence Bar Chart */}
                    <div>
                      <label
                        style={{
                          fontSize: "0.9rem",
                          opacity: 0.9,
                          display: "block",
                          marginBottom: "1rem",
                          fontWeight: "bold",
                        }}
                      >
                        Genre Sequence Timeline
                      </label>
                      <ResponsiveContainer width="100%" height={350}>
                        <BarChart
                          data={genreSequenceData}
                          margin={{ top: 20, right: 30, left: 20, bottom: 60 }}
                        >
                          <CartesianGrid
                            strokeDasharray="3 3"
                            stroke="rgba(255,255,255,0.1)"
                          />
                          <XAxis
                            dataKey="position"
                            stroke="#fff"
                            tick={{ fill: "#fff", fontSize: 12 }}
                            label={{
                              value: "Position in Song",
                              position: "insideBottom",
                              offset: -10,
                              fill: "#fff",
                            }}
                          />
                          <YAxis
                            stroke="#fff"
                            tick={{ fill: "#fff", fontSize: 12 }}
                            label={{
                              value: "Segment",
                              angle: -90,
                              position: "insideLeft",
                              fill: "#fff",
                            }}
                            hide={true}
                          />
                          <Tooltip
                            contentStyle={{
                              backgroundColor: "rgba(0,0,0,0.9)",
                              border: "1px solid rgba(255,255,255,0.3)",
                              borderRadius: "0.5rem",
                              color: "#fff",
                              padding: "10px",
                            }}
                            itemStyle={{ color: "#fff" }}
                            formatter={(value, name, props) => [
                              props.payload.genre,
                              "Genre",
                            ]}
                          />
                          <Legend
                            wrapperStyle={{
                              color: "#fff",
                              paddingTop: "10px",
                            }}
                            content={() => {
                              // Create custom legend showing genre colors
                              const uniqueGenres = [
                                ...new Set(
                                  genreSequenceData.map((d) => d.genre),
                                ),
                              ];
                              return (
                                <div
                                  style={{
                                    display: "flex",
                                    flexWrap: "wrap",
                                    justifyContent: "center",
                                    gap: "15px",
                                    paddingTop: "10px",
                                  }}
                                >
                                  {uniqueGenres.map((genre) => (
                                    <div
                                      key={genre}
                                      style={{
                                        display: "flex",
                                        alignItems: "center",
                                        gap: "5px",
                                      }}
                                    >
                                      <div
                                        style={{
                                          width: "12px",
                                          height: "12px",
                                          borderRadius: "50%",
                                          backgroundColor: genreToColor[genre],
                                        }}
                                      />
                                      <span
                                        style={{
                                          fontSize: "13px",
                                          color: "#fff",
                                        }}
                                      >
                                        {genre}
                                      </span>
                                    </div>
                                  ))}
                                </div>
                              );
                            }}
                          />
                          <Bar dataKey="value" radius={[8, 8, 0, 0]}>
                            {genreSequenceData.map((entry, index) => (
                              <Cell
                                key={`cell-${index}`}
                                fill={genreToColor[entry.genre]}
                              />
                            ))}
                          </Bar>
                        </BarChart>
                      </ResponsiveContainer>
                    </div>
                  </div>
                </div>
              )}
            </div>
          );
        })}
      </div>
    </section>
  );
};

export default Classifications;
