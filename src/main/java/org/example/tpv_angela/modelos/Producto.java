package org.example.tpv_angela.modelos;

/**
 * Modelo que representa un producto o plato disponible en la carta.
 */
public class Producto
{
    private String nombre;
    private String categoria;
    private String subcategoria;
    private double precio;
    private String tipo;
    private String imagen;
    private boolean disponible;

    /**
     * Crea un producto vacío para frameworks o cargas progresivas de datos.
     */
    public Producto() {}

    /**
     * Crea una instancia de Producto con los datos necesarios para su uso.
     * @param nombre nombre del producto o elemento.
     * @param categoria categoría del producto.
     * @param subcategoria subcategoría del producto.
     * @param precio precio del producto.
     * @param tipo tipo de documento o cobro.
     * @param imagen ruta o nombre de la imagen asociada.
     * @param disponible indica si el producto está disponible.
     */
    public Producto(String nombre, String categoria, String subcategoria, double precio, String tipo, String imagen, boolean disponible) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.subcategoria = subcategoria;
        this.precio = precio;
        this.tipo = tipo;
        this.imagen = imagen;
        this.disponible = disponible;
    }

    /**
     * Crea una instancia de Producto con los datos necesarios para su uso.
     * @param categoria categoría del producto.
     * @param nombre nombre del producto o elemento.
     * @param subcategoria subcategoría del producto.
     * @param precio precio del producto.
     * @param imagen ruta o nombre de la imagen asociada.
     * @param disponible indica si el producto está disponible.
     */
    public Producto(String categoria, String nombre, String subcategoria, double precio, String imagen, boolean disponible) {
        this.categoria = categoria;
        this.nombre = nombre;
        this.subcategoria = subcategoria;
        this.precio = precio;
        this.disponible = disponible;
    }

    //Getterss
    /**
     * Devuelve el nombre del producto.
     * @return nombre del producto.
     */
    public String getNombre() {return nombre;}
    /**
     * Devuelve la categoría del producto.
     * @return categoría del producto.
     */
    public String getCategoria() {return categoria;}
    /**
     * Devuelve la subcategoría del producto.
     * @return subcategoría del producto.
     */
    public String getSubcategoria() {return subcategoria;}
    /**
     * Devuelve el precio del producto.
     * @return precio del producto.
     */
    public double getPrecio() {return precio;}
    /**
     * Devuelve el tipo del producto.
     * @return tipo del producto.
     */
    public String getTipo() {return tipo;}
    /**
     * Devuelve la ruta o contenido de imagen del producto.
     * @return imagen del producto.
     */
    public String getImagen() {return imagen;}
    /**
     * Indica si el producto está disponible.
     * @return true si el producto está disponible; false en caso contrario.
     */
    public boolean isDisponible() {return disponible;}

    // Setters
    /**
     * Asigna el nombre del producto.
     * @param nombre nombre del producto.
     */
    public void setNombre(String nombre) {this.nombre = nombre;}
    /**
     * Asigna la disponibilidad del producto.
     * @param disponible disponibilidad del producto.
     */
    public void setDisponible(boolean disponible) {this.disponible = disponible;}
    /**
     * Asigna la imagen del producto.
     * @param imagen ruta o contenido de imagen del producto.
     */
    public void setImagen(String imagen) {this.imagen = imagen;}
    /**
     * Asigna el tipo del producto.
     * @param tipo tipo del producto.
     */
    public void setTipo(String tipo) {this.tipo = tipo;}
    /**
     * Asigna el precio del producto.
     * @param precio precio del producto.
     */
    public void setPrecio(double precio) {this.precio = precio;}
}
