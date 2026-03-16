package org.example.tpv_angela.modelos;

/**
 * Clase modelo de usuario que contiene los atributos necesarios del usuario
 */
public class Usuario
{
    /** Atributos para el usuario, en este caso sólamente tenemos el
     * usuario admin */
    private int id;
    private String userName;
    private String password;
    private int telefono;
    private String correo;

    /**
     * Constructor de usuario vacío
     */
    public Usuario() {}

    /**
     * Constructor de usuario con atributos
     * @param id
     * @param userName
     * @param password
     * @param telefono
     * @param correo
     */
    public Usuario(int id, String userName, String password, int telefono, String correo)
    {
        this.id = id;
        this.userName = userName;
        this.password = password;
        this.telefono = telefono;
        this.correo = correo;
    }

    /**
     * Getters de los atributos del usuario
     * @return
     */
    public int getId() {return id;}
    public String getUserName() {return userName;}
    public String getPassword() {return password;}
    public int getTelefono() {return telefono;}
    public String getCorreo() {return correo;}

    /**
     * Setterss de los atributos de usuario
     * @param
     */
    public void setId(int id) {this.id = id;}
    public void setUserName(String userName) {this.userName = userName;}
    public void setPassword(String password) {this.password = password;}
    public void setTelefono(int telefono) {this.telefono = telefono;}
    public void setCorreo(String correo) {this.correo = correo;}

    /**
     * Método to string que devuelve un usuario con sus atributos
     * @return
     */
    @Override
    public String toString()
    {
        return "Usuario{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", telefono=" + telefono +
                ", correo='" + correo + '\'' +
                '}';
    }
}
