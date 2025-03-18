package com.example

import io.ktor.server.application.*
import io.ktor.server.html.respondHtml
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import io.ktor.server.html.*
import kotlinx.html.*
import kotlinx.html.stream.appendHTML



fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Welcome to the main page!")
        }


        get("/register") {
            call.respondHtml {
                head {
                    title { +"Register" }
                    script {
                        unsafe {
                            +"""
                    document.addEventListener('DOMContentLoaded', function() {
                        const form = document.getElementById('registerForm');
                        form.addEventListener('submit', async function(event) {
                            event.preventDefault();

                            // Собираем данные из формы
                            const login = form.querySelector('input[name="login"]').value;
                            const password = form.querySelector('input[name="password"]').value;
                            const email = form.querySelector('input[name="email"]').value;

                            // Отправляем данные в формате JSON
                            const response = await fetch('/register', {
                                method: 'POST',
                                body: JSON.stringify({
                                    login: login,
                                    password: password,
                                    email: email
                                }),
                                headers: {
                                    'Content-Type': 'application/json'
                                }
                            });

                            // Обрабатываем ответ
                            if (response.ok) {
                                alert('Registration successful!');
                                window.location.href = '/login'; // Перенаправление на страницу входа
                            } else {
                                const errorText = await response.text();
                                alert('Registration failed: ' + errorText);
                            }
                        });
                    });
                    """
                        }
                    }
                }
                body {
                    h1 { +"Register" }
                    form(action = "/register", method = FormMethod.post) {
                        attributes["id"] = "registerForm" // Указываем атрибут id
                        label { +"login:" }
                        input(type = InputType.text, name = "login") { }
                        br
                        label { +"Password:" }
                        input(type = InputType.password, name = "password") { }
                        br
                        label { +"Email:" }
                        input(type = InputType.email, name = "email") { } // Добавляем поле для email
                        br
                        button(type = ButtonType.submit) { +"Register" }
                    }
                    p { +"Already have an account? " }
                    a(href = "/login") { +"Login here" }
                }
            }





            get("/login") {
                call.respondHtml {
                    head {
                        title { +"Login" }
                        script {
                            unsafe {
                                +"""
                    document.addEventListener('DOMContentLoaded', function() {
                        const form = document.getElementById('loginForm');
                        form.addEventListener('submit', async function(event) {
                            event.preventDefault();

                            const login = form.querySelector('input[name="login"]').value;
                            const password = form.querySelector('input[name="password"]').value;

                            const response = await fetch('/login', {
                                method: 'POST',
                                body: JSON.stringify({
                                    login: login,
                                    password: password
                                }),
                                headers: {
                                    'Content-Type': 'application/json'
                                }
                            });

                            if (response.ok) {
                                const result = await response.json();
                                alert('Login successful!');
                                localStorage.setItem('token', result.token); // Сохраняем токен
                                window.location.href = '/welcome'; // Переход на защищённую страницу
                            } else {
                                const errorText = await response.text();
                                alert('Login failed: ' + errorText);
                            }
                        });
                    });
                    """
                            }
                        }
                    }
                    body {
                        h1 { +"Login" }
                        form(action = "/login", method = FormMethod.post) {
                            attributes["id"] = "loginForm"
                            label { +"login:" }
                            input(type = InputType.text, name = "login") { }
                            br
                            label { +"Password:" }
                            input(type = InputType.password, name = "password") { }
                            br
                            button(type = ButtonType.submit) { +"Login" }
                        }
                        p { +"Don't have an account? " }
                        a(href = "/register") { +"Register here" }
                    }
                }
            }


        }
    }
}
