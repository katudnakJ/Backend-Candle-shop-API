API : http://localhost:8080/candle-shop-project-api/v1

SWAGGER : https://localhost:8443/candle-shop-project-api/swagger-ui/index.html#/

---
# Cors Configuration (09/02/2026)
ไปตั้งค่า ENV เพิ่มก่อน - ดูใน Discord (Backend - env & Password)
ถ้าไม่ได้ก็ลองทำตามข้างล่างนี้
### Run คำสั่งใน power shell โดยรันเป็น Administrator
choco install -y mkcert openssl (นานหน่อย พท. 248MB)
mkcert -install
mkcert localhost


# mkcert / local TLS artifacts
#### เอาไปใส่ใน .gitignore
*.pem
*-key.pem
*.p12
*.pfx
/src/main/resources/ssl/
/ssl/

# Run คำสั่งใน power shell โดยรันเป็น Administrator
openssl pkcs12 -export `
  -out localhost.p12 `
-inkey localhost-key.pem `
  -in localhost.pem `
-name localhost

## จากนั้นย้าย file .p12 ไปไว้ที่ /src/main/resources/ssl/ ก็เสร็จสิ้น
---
