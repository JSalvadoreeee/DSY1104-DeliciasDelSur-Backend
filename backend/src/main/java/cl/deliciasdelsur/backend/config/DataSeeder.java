package cl.deliciasdelsur.backend.config;

import cl.deliciasdelsur.backend.model.Categoria;
import cl.deliciasdelsur.backend.model.Producto;
import cl.deliciasdelsur.backend.model.Rol;
import cl.deliciasdelsur.backend.model.Usuario;
import cl.deliciasdelsur.backend.repository.CategoriaRepository;
import cl.deliciasdelsur.backend.repository.ProductoRepository;
import cl.deliciasdelsur.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

// Carga datos de prueba al iniciar, solo si las tablas están vacías
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            Usuario admin = Usuario.builder()
                    .nombre("Administrador Delicias")
                    .correo("admin@deliciasdelsur.cl")
                    .password(passwordEncoder.encode("admin123"))
                    .rol(Rol.ADMINISTRADOR)
                    .direccion("Av. Alemania 450, Temuco")
                    .telefono("+56911111111")
                    .estado(true)
                    .build();

            Usuario vendedor = Usuario.builder()
                    .nombre("Vendedor Sucursal")
                    .correo("vendedor@deliciasdelsur.cl")
                    .password(passwordEncoder.encode("vendedor123"))
                    .rol(Rol.VENDEDOR)
                    .direccion("Local Central, Valdivia")
                    .telefono("+56922222222")
                    .estado(true)
                    .build();

            Usuario cliente = Usuario.builder()
                    .nombre("Cliente Prueba")
                    .correo("cliente@deliciasdelsur.cl")
                    .password(passwordEncoder.encode("cliente123"))
                    .rol(Rol.CLIENTE)
                    .direccion("Los Carrera 890, Osorno")
                    .telefono("+56933333333")
                    .estado(true)
                    .build();

            usuarioRepository.saveAll(List.of(admin, vendedor, cliente));
        }

        if (categoriaRepository.count() == 0 && productoRepository.count() == 0) {
            Categoria conservas = categoriaRepository.save(Categoria.builder()
                    .nombre("Conservas")
                    .descripcion("Mermeladas y verduras en conserva del sur de Chile")
                    .build());

            Categoria salsas = categoriaRepository.save(Categoria.builder()
                    .nombre("Salsas")
                    .descripcion("Salsas caseras elaboradas con ingredientes frescos")
                    .build());

            Categoria deshidratados = categoriaRepository.save(Categoria.builder()
                    .nombre("Deshidratados")
                    .descripcion("Frutas deshidratadas sin conservantes")
                    .build());

            productoRepository.saveAll(List.of(
                    producto("Mermelada de Frutilla",
                            "Mermelada artesanal de frutilla. Vida útil: 365 días.",
                            2500, 40, 20,
                            "https://osojimix.com/wp-content/uploads/2021/04/MERMELADA-DE-FRUTILLAS.jpg",
                            conservas),
                    producto("Mermelada de Durazno",
                            "Mermelada artesanal de durazno. Vida útil: 365 días.",
                            2300, 35, 0,
                            "https://osojimix.com/wp-content/uploads/2022/01/Mermelada-de-duraznos-con-tagatosa-Pa%CC%81gina-500x500.jpg",
                            conservas),
                    producto("Salsa de Tomate",
                            "Salsa de tomate casera. Vida útil: 180 días.",
                            1800, 50, 17,
                            "https://www.aceitesdeolivadeespana.com/wp-content/uploads/2021/05/Salsa-de-tomate-casera.jpg",
                            salsas),
                    producto("Choclo en Conserva",
                            "Choclo tierno en conserva. Vida útil: 540 días.",
                            1200, 60, 0,
                            "https://www.wikihow.com/images_en/thumb/5/50/Store-Sweet-Corn-Step-16-Version-2.jpg/550px-nowatermark-Store-Sweet-Corn-Step-16-Version-2.jpg",
                            conservas),
                    producto("Arvejas en Conserva",
                            "Arvejas en conserva. Vida útil: 540 días.",
                            1100, 60, 0,
                            "https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEjgHRKaiUgwqqplO0kC9Hij4rTuj5bfzwQTCDa6Zmh8XZ46UY_hNNQSul3j73ruYonNK_X3bJuDvvFCK_CC8Z_VUMps_K9qhTn4f-3dGq1PVw1_0Ky7HFJQrktCeZbkQR4NaXMdYe00e6w/s1600/LATA+DE+ARVEJAS+VOLVANDO+A++PLATO.jpg",
                            conservas),
                    producto("Manzana Deshidratada",
                            "Manzana deshidratada sin conservantes. Vida útil: 180 días.",
                            3500, 25, 17,
                            "https://deshidratadordealimentos.cl/wp-content/uploads/2020/05/manzanas-1024x683.jpg",
                            deshidratados)
            ));
        }
    }

    private Producto producto(String nombre, String descripcion, int precio, int stock,
                              int oferta, String imagenUrl, Categoria categoria) {
        return Producto.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .precio(precio)
                .stock(stock)
                .oferta(oferta)
                .imagenUrl(imagenUrl)
                .categoria(categoria)
                .build();
    }
}