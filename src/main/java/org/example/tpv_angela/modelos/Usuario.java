package org.example.tpv_angela.modelos;

/**
 * Clase modelo de usuario que contiene los atributos necesarios del usuario
 */
public class Usuario
{
    /** Atributos para el usuario, en este caso sósolamente tenemos el
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
     * Constructor de usuario con atributos.
     * @param id identificador del usuario.
     * @param userName nombre de usuario.
     * @param password contraseña del usuario.
     * @param telefono telefono del usuario.
     * @param correo correo electrónico del usuario.
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
     * Devuelve el identificador del usuario.
     * @return identificador del usuario.
     */
    public int getId() {return id;}
    /**
     * Devuelve el nombre de usuario.
     * @return nombre de usuario.
     */
    public String getUserName() {return userName;}
    /**
     * Devuelve la contraseña del usuario.
     * @return contraseña del usuario.
     */
    public String getPassword() {return password;}
    /**
     * Devuelve el telefono del usuario.
     * @return telefono del usuario.
     */
    public int getTelefono() {return telefono;}
    /**
     * Devuelve el correo electrónico del usuario.
     * @return correo electrónico del usuario.
     */
    public String getCorreo() {return correo;}

    /**
     * Asigna el identificador del usuario.
     * @param id identificador del usuario.
     */
    public void setId(int id) {this.id = id;}
    /**
     * Asigna el nombre de usuario.
     * @param userName nombre de usuario.
     */
    public void setUserName(String userName) {this.userName = userName;}
    /**
     * Asigna la contraseña del usuario.
     * @param password contraseña del usuario.
     */
    public void setPassword(String password) {this.password = password;}
    /**
     * Asigna el telefono del usuario.
     * @param telefono telefono del usuario.
     */
    public void setTelefono(int telefono) {this.telefono = telefono;}
    /**
     * Asigna el correo electrónico del usuario.
     * @param correo correo electrónico del usuario.
     */
    public void setCorreo(String correo) {this.correo = correo;}

    /**
     * Método toString que devuelve un usuario con sus atributos.
     * @return representación textual del usuario.
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
