import { useEffect, useState } from "react";
import { Bell, Search, X } from "lucide-react";
import api from "../api/axios";

type Notification = {
  id: number;
  message: string;
  createdAt: string;
  read: boolean;
};

type CurrentUser = {
  id: number;
  name: string;
  email: string;
  role: string;
};

function Navbar() {
  const [search, setSearch] = useState("");

  const [showNotifications, setShowNotifications] =
    useState(false);

  const [notifications, setNotifications] =
    useState<Notification[]>([]);

  const [loadingNotifications, setLoadingNotifications] =
    useState(false);

  const [currentUser, setCurrentUser] =
    useState<CurrentUser | null>(null);

  const [unreadCount, setUnreadCount] =
    useState(0);



  const fetchCurrentUser = async () => {
    try {
      const response = await api.get("/me");

      console.log("Current User:", response.data);

      setCurrentUser(response.data);

    } catch (error: any) {
      console.error(
        "Failed to fetch current user:",
        error.response?.data || error
      );
    }
  };



  const fetchNotifications = async () => {
  try {
    setLoadingNotifications(true);

    const response = await api.get("/notifications");

    const data: Notification[] = response.data || [];

    setNotifications(data);

    const unread = data.filter(
      (notification) => !notification.read
    ).length;

    setUnreadCount(unread);

    return unread;

  } catch (error: any) {
    console.error(
      "Failed to fetch notifications:",
      error.response?.data || error
    );

    return 0;

  } finally {
    setLoadingNotifications(false);
  }
};



  useEffect(() => {

    fetchCurrentUser();

    fetchNotifications();

    // تحديث الإشعارات كل 10 ثواني
    const interval = setInterval(() => {

      fetchNotifications();

    }, 10000);


    return () => {

      clearInterval(interval);

    };

  }, []);



const handleNotificationClick = async () => {
  const willOpen = !showNotifications;

  setShowNotifications(willOpen);

  if (!willOpen) {
    return;
  }

  try {
    const unread = await fetchNotifications();

    if (unread > 0) {
      await api.put("/notifications/read");

      setNotifications((prev) =>
        prev.map((notification) => ({
          ...notification,
          read: true,
        }))
      );

      setUnreadCount(0);
    }

  } catch (error: any) {
    console.error(
      "Failed to mark notifications as read:",
      error.response?.data || error
    );
  }
};




  const logout = () => {

    localStorage.removeItem("token");

    window.location.href = "/login";
  };




  const displayName =
    currentUser?.name || "";

  const initial =
    displayName
      ? displayName
          .charAt(0)
          .toUpperCase()
      : "";

  const roleLabel =
    currentUser?.role === "ADMIN"
      ? "Administrator"
      : "User";


  return (
    <div
      className="
        h-[90px]
        px-8
        flex
        items-center
        justify-between
        relative
      "
    >


      <div>

        <h1 className="text-3xl font-black text-white">

          {displayName
            ? `Welcome, ${displayName} 👋`
            : "Welcome 👋"}

        </h1>

        <p className="text-gray-400 mt-1">
          Manage your requests professionally
        </p>

      </div>



      <div className="flex items-center gap-5">



        <div
          className="
            hidden
            lg:flex
            items-center
            gap-3
            bg-white/5
            border
            border-white/10
            px-5
            py-3
            rounded-2xl
            backdrop-blur-xl
            w-[320px]
          "
        >

          <Search
            size={18}
            className="text-gray-400"
          />

          <input
            type="text"
            placeholder="Search requests..."
            value={search}
            onChange={(e) =>
              setSearch(e.target.value)
            }
            className="
              bg-transparent
              outline-none
              text-white
              w-full
              placeholder:text-gray-500
            "
          />

          {search && (

            <button
              type="button"
              onClick={() =>
                setSearch("")
              }
            >

              <X
                size={16}
                className="
                  text-gray-500
                  hover:text-white
                  transition
                "
              />

            </button>

          )}

        </div>




        <div className="relative">

          <button
            type="button"
            onClick={handleNotificationClick}
            className="
              relative
              bg-white/5
              hover:bg-white/10
              border
              border-white/10
              p-4
              rounded-2xl
              transition-all
              duration-300
              backdrop-blur-xl
            "
          >

            <Bell size={20} />




            {unreadCount > 0 && (

              <div
                className="
                  absolute
                  -top-1
                  -right-1
                  min-w-[20px]
                  h-5
                  px-1
                  flex
                  items-center
                  justify-center
                  bg-red-500
                  rounded-full
                  text-white
                  text-[10px]
                  font-bold
                "
              >

                {unreadCount > 99
                  ? "99+"
                  : unreadCount}

              </div>

            )}

          </button>




          {showNotifications && (

            <div
              className="
                absolute
                top-16
                right-0
                w-[340px]
                bg-[#0f172a]
                border
                border-white/10
                rounded-3xl
                p-5
                shadow-2xl
                backdrop-blur-2xl
                z-50
              "
            >



              <div
                className="
                  flex
                  items-center
                  justify-between
                  mb-5
                "
              >

                <h2
                  className="
                    text-xl
                    font-black
                    text-white
                  "
                >
                  Notifications
                </h2>


                <span
                  className="
                    bg-blue-500/20
                    text-blue-400
                    text-xs
                    px-3
                    py-1
                    rounded-full
                  "
                >

                  {unreadCount} New

                </span>

              </div>



              {loadingNotifications && (

                <div
                  className="
                    text-center
                    text-gray-400
                    py-8
                  "
                >
                  Loading notifications...
                </div>

              )}



              {!loadingNotifications &&
                notifications.length === 0 && (

                  <div
                    className="
                      text-center
                      text-gray-400
                      py-8
                    "
                  >

                    <Bell
                      size={32}
                      className="
                        mx-auto
                        mb-3
                        opacity-40
                      "
                    />

                    <p>
                      No notifications yet
                    </p>

                  </div>

                )}



              {!loadingNotifications &&
                notifications.length > 0 && (

                  <div
                    className="
                      flex
                      flex-col
                      gap-4
                      max-h-[400px]
                      overflow-y-auto
                    "
                  >

                    {notifications.map(
                      (notification) => (

                        <div
                          key={notification.id}
                          className={`
                            p-4
                            rounded-2xl
                            border
                            transition

                            ${
                              !notification.read
                                ? "bg-blue-500/10 border-blue-500/30"
                                : "bg-white/5 border-white/5"
                            }

                            hover:bg-white/10
                          `}
                        >

                          <div
                            className="
                              flex
                              items-start
                              gap-3
                            "
                          >


                            <div
                              className={`
                                mt-1
                                w-2
                                h-2
                                rounded-full
                                flex-shrink-0

                                ${
                                  !notification.read
                                    ? "bg-blue-400"
                                    : "bg-gray-600"
                                }
                              `}
                            />


                            <div>

                              <p
                                className="
                                  text-sm
                                  text-white
                                  leading-relaxed
                                "
                              >
                                {notification.message}
                              </p>


                              {notification.createdAt && (

                                <p
                                  className="
                                    text-xs
                                    text-gray-500
                                    mt-2
                                  "
                                >

                                  {new Date(
                                    notification.createdAt
                                  ).toLocaleString()}

                                </p>

                              )}

                            </div>

                          </div>

                        </div>

                      )
                    )}

                  </div>

                )}

            </div>

          )}

        </div>



        <div
          className="
            hidden
            md:flex
            items-center
            gap-3
            bg-white/5
            border
            border-white/10
            px-5
            py-3
            rounded-2xl
            backdrop-blur-xl
          "
        >

          <div
            className="
              w-3
              h-3
              rounded-full
              bg-green-400
              animate-pulse
            "
          />

          <span className="text-sm text-gray-300">
            System Online
          </span>

        </div>



        <div
          className="
            flex
            items-center
            gap-3
            bg-white/5
            border
            border-white/10
            px-4
            py-2
            rounded-2xl
            backdrop-blur-xl
          "
        >

          <div
            className="
              w-11
              h-11
              rounded-2xl
              bg-gradient-to-br
              from-blue-500
              to-cyan-400
              flex
              items-center
              justify-center
              font-bold
              text-lg
              text-white
              shadow-lg
            "
          >

            {initial}

          </div>


          <div className="hidden md:block">

            <h3 className="font-semibold text-white">
              {displayName}
            </h3>

            <p className="text-xs text-gray-400">
              {roleLabel}
            </p>

          </div>

        </div>


        <button
          type="button"
          onClick={logout}
          className="
            bg-red-500
            hover:bg-red-600
            hover:scale-105
            transition-all
            duration-300
            px-5
            py-3
            rounded-2xl
            text-white
            font-semibold
            shadow-lg
            shadow-red-500/20
          "
        >
          Logout
        </button>

      </div>

    </div>
  );
}

export default Navbar;