package client_server.models;

    public class LoginResponseDto {

        private String token;
        private String login;

        public LoginResponseDto(String token, String login) {
            this.token = token;
            this.login = login;
        }
        public LoginResponseDto(){}

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

    }
