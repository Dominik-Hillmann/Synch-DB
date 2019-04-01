# chmod +x Encrypt.sh
php -r "echo password_hash('$1', PASSWORD_DEFAULT);";