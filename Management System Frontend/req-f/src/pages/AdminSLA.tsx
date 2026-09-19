import { useEffect, useState } from "react";
import api from "../api/axios";
import {
  AlertTriangle,
  CheckCircle,
  Clock,
  Flame,
} from "lucide-react";

type RiskLevel =
  | "WITHIN_SLA"
  | "AT_RISK"
  | "BREACHED";

interface SLAPrediction {
  requestId: number;
  title: string;
  category: string;
  priority: string;
  status: string;
  slaHours: number;
  elapsedHours: number;
  remainingMinutes: number;
  breachProbability: number;
  riskLevel: RiskLevel;
  reasons: string[];
}

interface Dashboard {
  withinSLA: number;
  atRisk: number;
  breached: number;
  totalRequests: number;
  criticalRequests: SLAPrediction[];
}

function AdminSLA() {

  const [dashboard, setDashboard] =
    useState<Dashboard | null>(null);

  const [loading, setLoading] =
    useState(true);

  useEffect(() => {

    fetchDashboard();

  }, []);

  const fetchDashboard = async () => {

    try {

      const response =
        await api.get("/sla/dashboard");

      setDashboard(response.data);

    } catch (error) {

      console.error(
        "Failed to load SLA dashboard",
        error
      );

    } finally {

      setLoading(false);

    }
  };

  if (loading) {

    return (
      <div className="p-8">
        Loading SLA Dashboard...
      </div>
    );
  }

  if (!dashboard) {

    return (
      <div className="p-8">
        Failed to load SLA Dashboard.
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 p-8">

      <div className="mb-8">

        <h1 className="text-3xl font-bold text-gray-900">
          SAHAB SLA CENTER
        </h1>

        <p className="mt-2 text-gray-500">
          AI-powered SLA monitoring and risk prediction
        </p>

      </div>

      {/* Statistics */}

      <div className="grid grid-cols-1 gap-6 md:grid-cols-4">

        <StatCard
          title="Within SLA"
          value={dashboard.withinSLA}
          icon={<CheckCircle size={24} />}
        />

        <StatCard
          title="At Risk"
          value={dashboard.atRisk}
          icon={<AlertTriangle size={24} />}
        />

        <StatCard
          title="Breached"
          value={dashboard.breached}
          icon={<Clock size={24} />}
        />

        <StatCard
          title="Total Active"
          value={dashboard.totalRequests}
          icon={<Flame size={24} />}
        />

      </div>

      {/* AI Predictions */}

      <div className="mt-10">

        <div className="mb-5">

          <h2 className="text-2xl font-bold">
            AI Predictions
          </h2>

          <p className="text-gray-500">
            Requests most likely to breach their SLA
          </p>

        </div>

        <div className="space-y-4">

          {dashboard.criticalRequests.length === 0 ? (

            <div className="rounded-xl bg-white p-8 text-center shadow">

              <CheckCircle
                className="mx-auto mb-3"
                size={40}
              />

              <p className="font-medium">
                No requests are currently at risk.
              </p>

            </div>

          ) : (

            dashboard.criticalRequests.map(
              (request) => (

                <div
                  key={request.requestId}
                  className="rounded-xl bg-white p-6 shadow"
                >

                  <div className="flex flex-col justify-between gap-4 md:flex-row">

                    <div>

                      <div className="flex items-center gap-3">

                        <span className="text-lg font-bold">
                          Request #{request.requestId}
                        </span>

                        {request.riskLevel ===
                          "BREACHED" ? (

                          <span className="rounded-full bg-red-100 px-3 py-1 text-sm font-semibold text-red-700">
                            BREACHED
                          </span>

                        ) : (

                          <span className="rounded-full bg-yellow-100 px-3 py-1 text-sm font-semibold text-yellow-700">
                            AT RISK
                          </span>

                        )}

                      </div>

                      <h3 className="mt-2 text-lg">
                        {request.title}
                      </h3>

                      <p className="mt-1 text-sm text-gray-500">
                        {request.category} ·{" "}
                        {request.priority}
                      </p>

                    </div>

                    <div className="text-right">

                      <div className="text-3xl font-bold">
                        {request.breachProbability}%
                      </div>

                      <div className="text-sm text-gray-500">
                        breach probability
                      </div>

                    </div>

                  </div>

                  {/* SLA */}

                  <div className="mt-6">

                    <div className="mb-2 flex justify-between text-sm">

                      <span>
                        {request.elapsedHours}h elapsed
                      </span>

                      <span>
                        SLA: {request.slaHours}h
                      </span>

                    </div>

                    <div className="h-3 overflow-hidden rounded-full bg-gray-200">

                      <div
                        className="h-full rounded-full"
                        style={{
                          width: `${Math.min(
                            100,
                            (request.elapsedHours /
                              request.slaHours) *
                              100
                          )}%`,
                        }}
                      />

                    </div>

                  </div>

                  {/* Reasons */}

                  <div className="mt-5">

                    <p className="mb-2 font-semibold">
                      Why?
                    </p>

                    <ul className="space-y-1 text-sm text-gray-600">

                      {request.reasons.map(
                        (reason, index) => (

                          <li key={index}>
                            • {reason}
                          </li>

                        )
                      )}

                    </ul>

                  </div>

                </div>

              )
            )

          )}

        </div>

      </div>

    </div>
  );
}

function StatCard({
  title,
  value,
  icon,
}: {
  title: string;
  value: number;
  icon: React.ReactNode;
}) {

  return (
    <div className="rounded-xl bg-white p-6 shadow">

      <div className="flex items-center justify-between">

        <div>

          <p className="text-sm text-gray-500">
            {title}
          </p>

          <p className="mt-2 text-3xl font-bold">
            {value}
          </p>

        </div>

        {icon}

      </div>

    </div>
  );
}

export default AdminSLA;