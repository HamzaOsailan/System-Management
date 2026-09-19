import { useEffect, useState } from "react";

import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import RequestModal from "../components/RequestModal";

import api from "../api/axios";

import {
  FileText,
  CheckCircle,
  Clock,
  XCircle,
} from "lucide-react";

import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Legend,
} from "recharts";

import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";
import toast from "react-hot-toast";
import { motion } from "framer-motion";

// Small dependency-free animated counter — replaces react-countup,
// which was returning an invalid element type in this project's build.
function AnimatedNumber({
  end,
  duration = 2,
}: {
  end: number;
  duration?: number;
}) {
  const [value, setValue] = useState(0);

  useEffect(() => {
    let startTime: number | null = null;
    let frame: number;

    const step = (timestamp: number) => {
      if (startTime === null) startTime = timestamp;
      const progress = Math.min(
        (timestamp - startTime) / (duration * 1000),
        1
      );
      setValue(Math.floor(progress * end));
      if (progress < 1) {
        frame = requestAnimationFrame(step);
      }
    };

    frame = requestAnimationFrame(step);

    return () => cancelAnimationFrame(frame);
  }, [end, duration]);

  return <>{value}</>;
}

function Dashboard() {
  const [loading, setLoading] = useState(true);

  const [total, setTotal] = useState(0);
  const [approved, setApproved] = useState(0);
  const [pending, setPending] = useState(0);
  const [rejected, setRejected] = useState(0);

  const [recentRequests, setRecentRequests] = useState<any[]>([]);

  const [selectedRequest, setSelectedRequest] =
    useState<any>(null);

  const [search, setSearch] = useState("");
  const [filter, setFilter] = useState("ALL");

  const [currentPage, setCurrentPage] = useState(1);

  const requestsPerPage = 5;



  const chartData = [
    {
      name: "Approved",
      value: approved,
    },
    {
      name: "Pending",
      value: pending,
    },
    {
      name: "Rejected",
      value: rejected,
    },
  ];

  const COLORS = [
    "#22c55e",
    "#eab308",
    "#ef4444",
  ];


  const fetchDashboardData = async () => {
  try {
    const token = localStorage.getItem("token");

    if (!token) {
      toast.error("Please login again");
      return;
    }

    let role = "USER";

    try {
      const payload = JSON.parse(
        atob(token.split(".")[1])
      );

      role = payload.role || "USER";
    } catch (error) {
      console.error("Invalid JWT:", error);
      toast.error("Invalid session");
      return;
    }


    const endpoint =
      role === "ADMIN"
        ? "/requests"
        : "/requests/my";

    const res = await api.get(endpoint);

    const data = Array.isArray(res.data)
      ? res.data
      : [];

    setRecentRequests(data);

    setTotal(data.length);

    const approvedCount = data.filter(
      (req: any) =>
        req.status === "APPROVED"
    ).length;

    const pendingCount = data.filter(
      (req: any) =>
        req.status === "PENDING"
    ).length;

    const rejectedCount = data.filter(
      (req: any) =>
        req.status === "REJECTED"
    ).length;

    setApproved(approvedCount);
    setPending(pendingCount);
    setRejected(rejectedCount);

  } catch (error: any) {

    console.log(
      "Dashboard Error:",
      error
    );

    console.log(
      "Response:",
      error.response
    );

    if (error.response?.status === 403) {
      toast.error(
        "You don't have permission to access these requests"
      );
    } else {
      toast.error(
        error.response?.data?.message ||
        "Failed to load requests"
      );
    }

  } finally {
    setLoading(false);
  }
};



  useEffect(() => {
    fetchDashboardData();

    const interval = setInterval(() => {
      fetchDashboardData();
    }, 15000);

    return () => {
      clearInterval(interval);
    };
  }, []);



  const filteredRequests = recentRequests.filter(
    (request: any) => {
      const title = request.title || "";

      const matchesSearch = title
        .toLowerCase()
        .includes(search.toLowerCase());

      const matchesFilter =
        filter === "ALL" ||
        request.status === filter;

      return matchesSearch && matchesFilter;
    }
  );


  useEffect(() => {
    setCurrentPage(1);
  }, [search, filter]);



  const indexOfLastRequest =
    currentPage * requestsPerPage;

  const indexOfFirstRequest =
    indexOfLastRequest - requestsPerPage;

  const currentRequests =
    filteredRequests.slice(
      indexOfFirstRequest,
      indexOfLastRequest
    );

  const totalPages =
    Math.ceil(
      filteredRequests.length /
      requestsPerPage
    );



  const exportPDF = () => {
    const doc = new jsPDF();

    doc.setFontSize(20);

    doc.text(
      "Requests Report",
      14,
      20
    );

    const tableData =
      filteredRequests.map(
        (request: any) => [
          request.id,
          request.title || "",
          request.status || "",
        ]
      );

    autoTable(doc, {
      startY: 30,
      head: [
        [
          "ID",
          "Title",
          "Status",
        ],
      ],
      body: tableData,
    });

    doc.save(
      "requests-report.pdf"
    );
  };



  if (loading) {
    return (
      <div
        className="
          min-h-screen
          bg-[#020617]
          flex
          items-center
          justify-center
        "
      >
        <div
          className="
            w-16
            h-16
            border-4
            border-blue-500
            border-t-transparent
            rounded-full
            animate-spin
          "
        />
      </div>
    );
  }



  return (
    <div
      className="
        flex
        bg-gradient-to-br
        from-[#020617]
        via-[#07122b]
        to-[#020617]
        min-h-screen
        text-white
      "
    >


      <Sidebar />


      <div className="flex-1">


        <Navbar />


        <div className="p-8">


          <div className="mb-10">

            <h1
              className="
                text-5xl
                font-black
                tracking-tight
              "
            >
              Dashboard
            </h1>

            <p
              className="
                text-gray-400
                mt-2
                text-lg
              "
            >
              Welcome back 👋
            </p>

          </div>

  

          <motion.div
            className="
              grid
              grid-cols-1
              md:grid-cols-2
              xl:grid-cols-4
              gap-6
            "
            initial="hidden"
            animate="show"
            variants={{
              hidden: {},
              show: {
                transition: {
                  staggerChildren: 0.15,
                },
              },
            }}
          >


            <motion.div
              whileHover={{
                scale: 1.05,
                y: -5,
              }}
              transition={{
                type: "spring",
                stiffness: 300,
              }}
              className="
                bg-gradient-to-br
                from-blue-500/10
                to-blue-900/10
                border
                border-blue-500/10
                backdrop-blur-xl
                rounded-[32px]
                p-7
                shadow-2xl
              "
            >

              <div className="flex items-center justify-between">

                <div>

                  <p className="text-gray-400">
                    Total Requests
                  </p>

                  <h2
                    className="
                      text-5xl
                      font-black
                      mt-4
                    "
                  >
                    <AnimatedNumber
                      end={total}
                      duration={2}
                    />
                  </h2>

                </div>

                <div
                  className="
                    bg-blue-500/20
                    p-5
                    rounded-3xl
                  "
                >
                  <FileText size={34} />
                </div>

              </div>

            </motion.div>


            <motion.div
              whileHover={{
                scale: 1.05,
                y: -5,
              }}
              transition={{
                type: "spring",
                stiffness: 300,
              }}
              className="
                bg-gradient-to-br
                from-green-500/10
                to-green-900/10
                border
                border-green-500/10
                backdrop-blur-xl
                rounded-[32px]
                p-7
                shadow-2xl
              "
            >

              <div className="flex items-center justify-between">

                <div>

                  <p className="text-gray-400">
                    Approved
                  </p>

                  <h2
                    className="
                      text-5xl
                      font-black
                      mt-4
                      text-green-400
                    "
                  >
                    <AnimatedNumber
                      end={approved}
                      duration={2}
                    />
                  </h2>

                </div>

                <div
                  className="
                    bg-green-500/20
                    p-5
                    rounded-3xl
                  "
                >
                  <CheckCircle size={34} />
                </div>

              </div>

            </motion.div>


            <motion.div
              whileHover={{
                scale: 1.05,
                y: -5,
              }}
              transition={{
                type: "spring",
                stiffness: 300,
              }}
              className="
                bg-gradient-to-br
                from-yellow-500/10
                to-yellow-900/10
                border
                border-yellow-500/10
                backdrop-blur-xl
                rounded-[32px]
                p-7
                shadow-2xl
              "
            >

              <div className="flex items-center justify-between">

                <div>

                  <p className="text-gray-400">
                    Pending
                  </p>

                  <h2
                    className="
                      text-5xl
                      font-black
                      mt-4
                      text-yellow-300
                    "
                  >
                    <AnimatedNumber
                      end={pending}
                      duration={2}
                    />
                  </h2>

                </div>

                <div
                  className="
                    bg-yellow-500/20
                    p-5
                    rounded-3xl
                  "
                >
                  <Clock size={34} />
                </div>

              </div>

            </motion.div>


            <motion.div
              whileHover={{
                scale: 1.05,
                y: -5,
              }}
              transition={{
                type: "spring",
                stiffness: 300,
              }}
              className="
                bg-gradient-to-br
                from-red-500/10
                to-red-900/10
                border
                border-red-500/10
                backdrop-blur-xl
                rounded-[32px]
                p-7
                shadow-2xl
              "
            >

              <div className="flex items-center justify-between">

                <div>

                  <p className="text-gray-400">
                    Rejected
                  </p>

                  <h2
                    className="
                      text-5xl
                      font-black
                      mt-4
                      text-red-400
                    "
                  >
                    <AnimatedNumber
                      end={rejected}
                      duration={2}
                    />
                  </h2>

                </div>

                <div
                  className="
                    bg-red-500/20
                    p-5
                    rounded-3xl
                  "
                >
                  <XCircle size={34} />
                </div>

              </div>

            </motion.div>

          </motion.div>


          <div
            className="
              grid
              grid-cols-1
              xl:grid-cols-2
              gap-6
              mt-10
            "
          >


            <div
              className="
                bg-white/[0.03]
                border
                border-white/10
                rounded-[32px]
                p-8
                backdrop-blur-2xl
              "
            >

              <h2
                className="
                  text-3xl
                  font-black
                  mb-8
                "
              >
                Requests Overview
              </h2>

              <div className="h-[350px]">

                <ResponsiveContainer
                  width="100%"
                  height="100%"
                >

                  <PieChart>

                    <Pie
                      data={chartData}
                      dataKey="value"
                      nameKey="name"
                      cx="50%"
                      cy="50%"
                      innerRadius={70}
                      outerRadius={120}
                      paddingAngle={5}
                    >

                      {chartData.map(
                        (entry, index) => (
                          <Cell
                            key={index}
                            fill={COLORS[index]}
                          />
                        )
                      )}

                    </Pie>

                    <Tooltip />

                    <Legend />

                  </PieChart>

                </ResponsiveContainer>

              </div>

            </div>

 

            <div
              className="
                bg-white/5
                border
                border-white/10
                rounded-[32px]
                p-8
                backdrop-blur-2xl
              "
            >

              <h2
                className="
                  text-3xl
                  font-black
                  mb-8
                "
              >
                Requests Analytics
              </h2>

              <div className="h-[350px]">

                <ResponsiveContainer
                  width="100%"
                  height="100%"
                >

                  <BarChart
                    data={chartData}
                    margin={{
                      top: 20,
                      right: 30,
                      left: 0,
                      bottom: 5,
                    }}
                  >

                    <CartesianGrid
                      strokeDasharray="3 3"
                    />

                    <XAxis dataKey="name" />

                    <YAxis />

                    <Tooltip />

                    <Bar
                      dataKey="value"
                      radius={[
                        10,
                        10,
                        0,
                        0,
                      ]}
                    >

                      {chartData.map(
                        (entry, index) => (
                          <Cell
                            key={index}
                            fill={COLORS[index]}
                          />
                        )
                      )}

                    </Bar>

                  </BarChart>

                </ResponsiveContainer>

              </div>

            </div>

          </div>



          <div
            className="
              flex
              flex-col
              md:flex-row
              gap-4
              mt-10
              mb-6
            "
          >

            <input
              type="text"
              placeholder="Search requests..."
              value={search}
              onChange={(e) =>
                setSearch(e.target.value)
              }
              className="
                bg-white/5
                border
                border-white/10
                rounded-2xl
                px-5
                py-3
                text-white
                outline-none
                flex-1
              "
            />

            <select
              value={filter}
              onChange={(e) =>
                setFilter(e.target.value)
              }
              className="
                bg-[#0f172a]
                border
                border-white/10
                rounded-2xl
                px-5
                py-3
                text-white
                outline-none
              "
            >

              <option value="ALL">
                All
              </option>

              <option value="APPROVED">
                Approved
              </option>

              <option value="PENDING">
                Pending
              </option>

              <option value="REJECTED">
                Rejected
              </option>

            </select>

          </div>


          <div
            className="
              mt-10
              bg-white/5
              border
              border-white/10
              backdrop-blur-2xl
              rounded-[40px]
              p-8
              shadow-2xl
            "
          >

            <div
              className="
                flex
                items-center
                justify-between
                mb-8
              "
            >

              <div>

                <h2
                  className="
                    text-4xl
                    font-black
                  "
                >
                  Recent Requests
                </h2>

                <p className="text-gray-400 mt-2">
                  Latest activity inside the system
                </p>

              </div>

              <button
                onClick={exportPDF}
                className="
                  bg-gradient-to-r
                  from-blue-500
                  to-cyan-500
                  hover:scale-105
                  transition-all
                  duration-300
                  px-6
                  py-3
                  rounded-2xl
                  font-semibold
                  shadow-lg
                "
              >
                Export PDF
              </button>

            </div>


            {filteredRequests.length === 0 ? (

              <div
                className="
                  text-center
                  py-20
                  text-gray-400
                "
              >

                <h2
                  className="
                    text-2xl
                    font-bold
                  "
                >
                  No Requests Found
                </h2>

                <p className="mt-2">
                  Try changing filters or create new requests
                </p>

              </div>

            ) : (

              <>


                <div className="flex flex-col gap-5">

                  {currentRequests.map(
                    (request: any) => (
                      <motion.div
                        key={request.id}
                        initial={{
                          opacity: 0,
                          y: 20,
                        }}
                        animate={{
                          opacity: 1,
                          y: 0,
                        }}
                        className="
                          bg-white/5
                          border
                          border-white/10
                          rounded-[28px]
                          p-6
                        "
                      >
                        <div className="flex items-center justify-between">

                          <div>
                            <h2 className="text-2xl font-bold">
                              {request.title || "Untitled Request"}
                            </h2>

                            <p className="text-gray-400 mt-2">
                              {request.description || "No description"}
                            </p>
                          </div>

                          <span className="px-4 py-2 rounded-2xl bg-blue-500/20 text-blue-400">
                            {request.status}
                          </span>

                        </div>

                        <button
                          onClick={() => setSelectedRequest(request)}
                          className="
                            mt-5
                            bg-blue-500
                            hover:bg-blue-600
                            px-5
                            py-2
                            rounded-2xl
                            font-semibold
                          "
                        >
                          View
                        </button>

                      </motion.div>
                    )
                  )}

                </div>

 
                {totalPages > 1 && (

                  <div
                    className="
                      flex
                      items-center
                      justify-center
                      gap-4
                      mt-8
                    "
                  >

                    <button
                      disabled={currentPage === 1}
                      onClick={() =>
                        setCurrentPage(
                          (prev) => prev - 1
                        )
                      }
                      className="
                        px-5
                        py-2
                        rounded-2xl
                        bg-white/10
                        border
                        border-white/10
                        disabled:opacity-40
                        hover:bg-white/20
                        transition
                      "
                    >
                      Previous
                    </button>

                    <span className="text-gray-300">
                      Page {currentPage} of {totalPages}
                    </span>

                    <button
                      disabled={
                        currentPage === totalPages
                      }
                      onClick={() =>
                        setCurrentPage(
                          (prev) => prev + 1
                        )
                      }
                      className="
                        px-5
                        py-2
                        rounded-2xl
                        bg-white/10
                        border
                        border-white/10
                        disabled:opacity-40
                        hover:bg-white/20
                        transition
                      "
                    >
                      Next
                    </button>

                  </div>

                )}

              </>

            )}

          </div>

        </div>

      </div>


      {selectedRequest && (

        <RequestModal
          request={selectedRequest}
          onClose={() =>
            setSelectedRequest(null)
          }
        />

      )}

    </div>
  );
}

export default Dashboard;