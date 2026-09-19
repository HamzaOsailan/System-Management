
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  ArrowLeft,
  Sparkles,
  Loader2,
  AlertCircle,
  CheckCircle2,
  Building2,
  Gauge,
  Layers3,
  Hash,
} from "lucide-react";

import api from "../api/axios";

const INFO_STYLES = {
  category:
    "bg-cyan-500/10 text-cyan-400 border-cyan-500/20",

  subCategory:
    "bg-indigo-500/10 text-indigo-400 border-indigo-500/20",

  department:
    "bg-blue-500/10 text-blue-400 border-blue-500/20",

  urgency:
    "bg-amber-500/10 text-amber-400 border-amber-500/20",
};

function AdminAIAnalysis() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [analysis, setAnalysis] = useState(null);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState("");

  const headers = {
    Authorization: `Bearer ${localStorage.getItem("token")}`,
  };

  // =====================================================
  // FETCH
  // =====================================================

  const fetchAnalysis = async () => {
    try {
      setLoading(true);
      setError("");

      const { data } = await api.get(
        `/requests/${id}/ai-analysis/admin`,
        { headers }
      );

      setAnalysis(data);
    } catch (err) {
      console.error(err);

      if (err.response?.status === 404) {
        setAnalysis(null);
      } else {
        setError(
          err.response?.data?.message ||
            "Failed to load AI analysis."
        );
      }
    } finally {
      setLoading(false);
    }
  };

  // =====================================================
  // GENERATE
  // =====================================================

  const generateAnalysis = async () => {
    try {
      setGenerating(true);
      setError("");

      const { data } = await api.post(
        `/requests/${id}/ai-analysis/generate`,
        {},
        { headers }
      );

      setAnalysis(data);
    } catch (err) {
      console.error(err);

      setError(
        err.response?.data?.message ||
          "Failed to generate AI analysis."
      );
    } finally {
      setGenerating(false);
    }
  };

  useEffect(() => {
    fetchAnalysis();
  }, [id]);

  // =====================================================
  // LOADING
  // =====================================================

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 text-white flex items-center justify-center">
        <div className="flex items-center gap-3 text-slate-400">
          <Loader2
            size={22}
            className="animate-spin"
          />
          Loading AI analysis...
        </div>
      </div>
    );
  }

  // =====================================================
  // NO ANALYSIS
  // =====================================================

  if (!analysis) {
    return (
      <div className="min-h-screen bg-slate-950 text-white p-6">
        <button
          type="button"
          onClick={() => navigate(-1)}
          className="
            inline-flex items-center gap-2
            text-slate-400
            hover:text-white
            transition
            mb-8
          "
        >
          <ArrowLeft size={19} />
          Back
        </button>

        <div className="max-w-3xl mx-auto">
          <div className="
            bg-white/5
            border border-white/10
            rounded-[28px]
            p-10
            text-center
          ">
            <div className="
              w-16 h-16
              mx-auto
              rounded-2xl
              bg-purple-500/10
              border border-purple-500/20
              flex items-center justify-center
              mb-5
            ">
              <Sparkles
                size={30}
                className="text-purple-400"
              />
            </div>

            <h1 className="text-2xl font-bold">
              AI Analysis Not Generated
            </h1>

            <p className="text-slate-400 mt-2 max-w-md mx-auto">
              This approved request does not have an AI
              analysis yet.
            </p>

            {error && (
              <div className="
                flex items-center justify-center gap-2
                text-red-400
                text-sm
                mt-5
              ">
                <AlertCircle size={17} />
                {error}
              </div>
            )}

            <button
              type="button"
              onClick={generateAnalysis}
              disabled={generating}
              className="
                inline-flex items-center gap-2
                mt-7
                px-5 py-2.5
                rounded-2xl
                bg-purple-600
                text-white
                font-semibold
                hover:bg-purple-700
                transition
                disabled:opacity-50
                disabled:cursor-not-allowed
              "
            >
              {generating ? (
                <>
                  <Loader2
                    size={18}
                    className="animate-spin"
                  />
                  Generating...
                </>
              ) : (
                <>
                  <Sparkles size={18} />
                  Generate AI Analysis
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    );
  }

  // =====================================================
  // PAGE
  // =====================================================

  return (
    <div className="min-h-screen bg-slate-950 text-white p-4 sm:p-6 lg:p-8">
      <div className="max-w-6xl mx-auto">

        {/* BACK */}
        <button
          type="button"
          onClick={() => navigate(-1)}
          className="
            inline-flex items-center gap-2
            text-slate-400
            hover:text-white
            transition
            mb-7
          "
        >
          <ArrowLeft size={19} />
          Back
        </button>

        {/* HEADER */}
        <div className="
          flex flex-col sm:flex-row
          sm:items-center
          sm:justify-between
          gap-4
          mb-7
        ">
          <div>
            <div className="flex items-center gap-3">
              <div className="
                w-11 h-11
                rounded-xl
                bg-purple-500/10
                border border-purple-500/20
                flex items-center justify-center
              ">
                <Sparkles
                  size={22}
                  className="text-purple-400"
                />
              </div>

              <div>
                <h1 className="text-2xl sm:text-3xl font-bold">
                  AI Request Analysis
                </h1>

                <p className="text-slate-400 text-sm mt-1">
                  AI-generated analysis for Request #
                  {analysis.requestId}
                </p>
              </div>
            </div>
          </div>

          <div className="
            inline-flex items-center gap-2
            px-3 py-2
            rounded-xl
            bg-green-500/10
            border border-green-500/20
            text-green-400
            text-sm font-semibold
          ">
            <CheckCircle2 size={16} />
            Analysis Generated
          </div>
        </div>

        {/* MAIN CARD */}
        <div className="
          bg-white/5
          border border-white/10
          rounded-[28px]
          overflow-hidden
        ">

          {/* TOP SECTION */}
          <div className="p-5 sm:p-6 border-b border-white/10">

            <div className="
              grid
              grid-cols-1
              sm:grid-cols-2
              lg:grid-cols-3
              gap-4
            ">

              <InfoCard
                icon={Hash}
                label="Request ID"
                value={analysis.requestId}
              />

              <InfoCard
                icon={Layers3}
                label="Category"
                value={analysis.category}
                badgeStyle={INFO_STYLES.category}
              />

              <InfoCard
                icon={Layers3}
                label="Sub Category"
                value={analysis.subCategory}
                badgeStyle={INFO_STYLES.subCategory}
              />

              <InfoCard
                icon={Gauge}
                label="Priority"
                value={analysis.priority}
                badgeStyle={getPriorityStyle(
                  analysis.priority
                )}
              />

              <InfoCard
                icon={Gauge}
                label="Urgency"
                value={
                  analysis.urgency
                    ? `${analysis.urgency} / 5`
                    : "N/A"
                }
                badgeStyle={INFO_STYLES.urgency}
              />

              <InfoCard
                icon={Building2}
                label="Suggested Department"
                value={analysis.suggestedDepartment}
                badgeStyle={INFO_STYLES.department}
              />

            </div>
          </div>

          {/* ANALYSIS */}
          <div className="p-5 sm:p-6 space-y-4">

            <AnalysisSection
              title="Summary"
              content={analysis.summary}
            />

            <AnalysisSection
              title="Suggested Action"
              content={analysis.suggestedAction}
            />

            <AnalysisSection
              title="AI Reason"
              content={analysis.reason}
              highlight
            />

          </div>

          {/* FOOTER */}
          <div className="
            px-5 py-4
            sm:px-6
            border-t border-white/10
            text-xs text-slate-500
          ">
            Created at:{" "}
            {analysis.createdAt
              ? new Date(
                  analysis.createdAt
                ).toLocaleString()
              : "N/A"}
          </div>
        </div>
      </div>
    </div>
  );
}

// =====================================================
// INFO CARD
// =====================================================

function InfoCard({
  icon: Icon,
  label,
  value,
  badgeStyle,
}) {
  return (
    <div className="
      bg-white/5
      border border-white/10
      rounded-2xl
      p-4
    ">
      <div className="flex items-center gap-2 mb-3">
        <Icon
          size={15}
          className="text-slate-500"
        />

        <p className="text-xs uppercase tracking-wide text-slate-500">
          {label}
        </p>
      </div>

      {badgeStyle ? (
        <span
          className={`
            inline-flex
            px-3 py-1.5
            rounded-xl
            text-sm
            font-semibold
            border
            ${badgeStyle}
          `}
        >
          {value || "N/A"}
        </span>
      ) : (
        <p className="text-lg font-bold text-white">
          {value || "N/A"}
        </p>
      )}
    </div>
  );
}

// =====================================================
// ANALYSIS SECTION
// =====================================================

function AnalysisSection({
  title,
  content,
  highlight = false,
}) {
  return (
    <section
      className={`
        rounded-2xl
        border
        p-5
        ${
          highlight
            ? "bg-purple-500/5 border-purple-500/20"
            : "bg-white/5 border-white/10"
        }
      `}
    >
      <h2
        className={`
          text-base
          font-semibold
          mb-2
          ${
            highlight
              ? "text-purple-300"
              : "text-slate-200"
          }
        `}
      >
        {title}
      </h2>

      <p className="text-slate-400 leading-7 whitespace-pre-wrap">
        {content || "N/A"}
      </p>
    </section>
  );
}

// =====================================================
// PRIORITY COLOR
// =====================================================

function getPriorityStyle(priority) {
  switch (priority) {
    case "HIGH":
      return "bg-red-500/10 text-red-400 border-red-500/20";

    case "MEDIUM":
      return "bg-yellow-500/10 text-yellow-400 border-yellow-500/20";

    case "LOW":
      return "bg-green-500/10 text-green-400 border-green-500/20";

    default:
      return "bg-white/5 text-slate-300 border-white/10";
  }
}

export default AdminAIAnalysis;

