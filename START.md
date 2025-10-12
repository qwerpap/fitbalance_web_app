
```bash
./start.sh

http://localhost:8080/auth/test
http://localhost:8080/static/google-auth-test.html
```


### Публичные
- `GET /` - главная страница
- `POST /login` - логин
- `POST /register` - регистрация
- `GET /auth/google/login` - начало OAuth процесса
- `GET /auth/google/callback` - OAuth callback

### Защищенные (требуют JWT токен)
- `GET /calculator` - фитнес калькулятор
- `GET /user/info` - информация о пользователе

### Admin только
- `GET /admin/users` - список всех пользователей
- `PUT /admin/users/{id}/role` - изменить роль пользователя

