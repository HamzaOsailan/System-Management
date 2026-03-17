import React, { useState } from 'react'
import api from "../api/axios"


function Login() {

    const [username, SetUsername] = useState("");
    const [password, SetPassword] = useState("");

    const navigate = userNavigate();

    const login = async () => {
        try {
            const res = await api.post("/auth/login", {
                username,
                password
            });

            localStorage.setItem('token', res.data.token);
            navigate('/dashboard')
        } catch (error) {
            alert("Login failed");
        }
    }

    return (

        <div>
            <h2>Login</h2>
            <input
                placeholder="username"
                onChange={(e) => setUsername(e.target.value)}
            />

            <input
                type='password'
                placeholder='password'
                onChange={(e) => { SetPassword(e.target.value) }}
            />

            <button onClick={Login} />
        </div>



    )
}

export default Login