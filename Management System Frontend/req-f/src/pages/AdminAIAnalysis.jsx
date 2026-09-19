
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  ArrowLeft,
  Sparkles,
  Loader2,
  AlertCircle,
} from "lucide-react";
import api from "../api/axios";

function AdminAIAnalysis() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [analysis, setAnalysis] = useState(null);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState("");

  const token = localStorage.getItem("token");

  const headers = {
    Authorization: `Bearer ${token}`,
  };

  // ==============================
  // GET EXISTING AI ANALYSIS
  // ==============================
  const fetchAnalysis = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get(
        `/requests/${id}/ai-analysis/admin`,
        {
          headers,
        }
      );

      setAnalysis(response.data);
    } catch (err) {
      console.error("Failed to fetch AI analysis:", err);

      // Analysis does not exist yet
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

  // ==============================
  // GENERATE AI ANALYSIS
  // ==============================
  const generateAnalysis = async () => {
    try {
      setGenerating(true);
      setError("");

      const response = await api.post(
        `/requests/${id}/ai-analysis/generate`,
        {},
        {
          headers,
        }
      );

      setAnalysis(response.data);
    } catch (err) {
      console.error("Failed to generate AI analysis:", err);

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

  // ==============================
  // LOADING
  // ==============================
  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 text-white flex items-center justify-center">
        <div className="flex items-center gap-3 text-slate-300">
          <Loader2 className="animate-spin" size={24} />
          Loading AI analysis...
        </div>
      </div>
    );
  }

  // ==============================
  // NO ANALYSIS
  // ==============================
  if (!analysis) {
    return (
      <div className="min-h-screen bg-slate-950 text-white p-6">

        <button
          onClick={() => navigate(-1)}
          className="flex items-center gap-2 text-slate-300 hover:text-white mb-8"
        >
          <ArrowLeft size={20} />
          Back
        </button>

        <div className="max-w-3xl mx-auto">

          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-8 text-center">

            <div className="flex justify-center mb-5">
              <div className="p-4 rounded-full bg-purple-500/10">
                <Sparkles
                  size={40}
                  className="text-purple-400"
                />
              </div>
            </div>

            <h1 className="text-2xl font-bold mb-3">
              AI Analysis Not Generated
            </h1>

            <p className="text-slate-400 mb-6">
              This approved request does not have an AI analysis yet.
            </p>

            {error && (
              <div className="flex items-center gap-2 justify-center text-red-400 mb-5">
                <AlertCircle size={18} />
                <span>{error}</span>
              </div>
            )}

            <button
              onClick={generateAnalysis}
              disabled={generating}
              className="inline-flex items-center gap-2 px-6 py-3 rounded-xl bg-purple-600 hover:bg-purple-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
            >
              {generating ? (
                <>
                  <Loader2
                    size={20}
                    className="animate-spin"
                  />
                  Generating...
                </>
              ) : (
                <>
                  <Sparkles size={20} />
                  Generate AI Analysis
                </>
              )}
            </button>

          </div>
        </div>
      </div>
    );
  }

  // ==============================
  // ANALYSIS EXISTS
  // ==============================
  return (
    <div className="min-h-screen bg-slate-950 text-white p-6">

      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-slate-300 hover:text-white mb-8"
      >
        <ArrowLeft size={20} />
        Back
      </button>

      <div className="max-w-5xl mx-auto">

        {/* HEADER */}
        <div className="mb-8">

          <div className="flex items-center gap-3 mb-2">
            <Sparkles
              size={30}
              className="text-purple-400"
            />

            <h1 className="text-3xl font-bold">
              AI Request Analysis
            </h1>
          </div>

          <p className="text-slate-400">
            AI-generated analysis for Request #{analysis.requestId}
          </p>

        </div>

        {/* BASIC INFORMATION */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5 mb-6">

          <InfoCard
            label="Request ID"
            value={analysis.requestId}
          />

          <InfoCard
            label="Category"
            value={analysis.category}
          />

          <InfoCard
            label="Sub Category"
            value={analysis.subCategory}
          />

          <InfoCard
            label="Priority"
            value={analysis.priority}
            priority
          />

          <InfoCard
            label="Urgency"
            value={
              analysis.urgency
                ? `${analysis.urgency} / 5`
                : "N/A"
            }
          />

          <InfoCard
            label="Suggested Department"
            value={analysis.suggestedDepartment}
          />

        </div>

        {/* SUMMARY */}
        <AnalysisCard
          title="Summary"
          content={analysis.summary}
        />

        {/* SUGGESTED ACTION */}
        <AnalysisCard
          title="Suggested Action"
          content={analysis.suggestedAction}
        />

        {/* REASON */}
        <AnalysisCard
          title="AI Reason"
          content={analysis.reason}
        />

        {/* CREATED AT */}
        <div className="mt-6 text-sm text-slate-500">
          Created at:{" "}
          {analysis.createdAt
            ? new Date(
                analysis.createdAt
              ).toLocaleString()
            : "N/A"}
        </div>

      </div>
    </div>
  );
}

// =================================
// INFO CARD
// =================================
function InfoCard({
  label,
  value,
  priority = false,
}) {
  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5">

      <p className="text-sm text-slate-500 mb-2">
        {label}
      </p>

      {priority ? (
        <span
          className={`inline-flex px-3 py-1 rounded-full text-sm font-medium ${
            value === "HIGH"
              ? "bg-red-500/10 text-red-400"
              : value === "MEDIUM"
              ? "bg-yellow-500/10 text-yellow-400"
              : "bg-green-500/10 text-green-400"
          }`}
        >
          {value || "N/A"}
        </span>
      ) : (
        <p className="text-lg font-semibold text-white">
          {value || "N/A"}
        </p>
      )}

    </div>
  );
}

// =================================
// ANALYSIS CARD
// =================================
function AnalysisCard({
  title,
  content,
}) {
  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 mb-5">

      <h2 className="text-lg font-semibold mb-3 text-purple-300">
        {title}
      </h2>

      <p className="text-slate-300 leading-7 whitespace-pre-wrap">
        {content || "N/A"}
      </p>

    </div>
  );
}

export default AdminAIAnalysis;
