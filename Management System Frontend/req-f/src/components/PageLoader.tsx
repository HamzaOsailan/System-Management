function PageLoader() {

  return (

    <div className="fixed inset-0 bg-[#020617] flex items-center justify-center z-[9999] overflow-hidden">

      <div className="absolute w-[400px] h-[400px] bg-blue-500/20 rounded-full blur-3xl animate-pulse"></div>

      <div className="absolute w-[300px] h-[300px] bg-cyan-500/10 rounded-full blur-3xl animate-pulse"></div>

      <div className="relative flex flex-col items-center">

        <div className="w-24 h-24 border-[6px] border-blue-500/20 border-t-blue-500 rounded-full animate-spin"></div>

        <h1 className="text-white text-3xl font-black mt-8">
          Request System
        </h1>

        <p className="text-gray-400 mt-3">
          Loading dashboard...
        </p>

      </div>

    </div>

  );

}

export default PageLoader;