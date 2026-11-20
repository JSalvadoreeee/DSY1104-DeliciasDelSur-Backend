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

        if (categoriaRepository.count() == 0) {
            Categoria tortas = categoriaRepository.save(Categoria.builder()
                    .nombre("Tortas y Pasteles")
                    .descripcion("Repostería artesanal tradicional del sur de Chile")
                    .build());

            Categoria chocolates = categoriaRepository.save(Categoria.builder()
                    .nombre("Chocolatería")
                    .descripcion("Chocolates artesanales con frutos del bosque")
                    .build());

            Categoria mermeladas = categoriaRepository.save(Categoria.builder()
                    .nombre("Mermeladas y Conservas")
                    .descripcion("Preparaciones caseras con frutas de temporada")
                    .build());

            if (productoRepository.count() == 0) {
                productoRepository.saveAll(List.of(
                        Producto.builder()
                                .nombre("Kuchen de Murta y Frambuesa")
                                .descripcion("Masa quebrada artesanal rellena de murta silvestre y frambuesas frescas.")
                                .precio(14990)
                                .stock(15)
                                .oferta(10)
                                .imagenUrl("https://images.unsplash.com/photo-1565958011703-44f9829ba187?w=600")
                                .categoria(tortas)
                                .build(),
                        Producto.builder()
                                .nombre("Torta Selva Negra Sureña")
                                .descripcion("Bizcocho de cacao húmedo con crema chantilly, guindas ácidas y virutas de chocolate.")
                                .precio(22990)
                                .stock(10)
                                .oferta(0)
                                .imagenUrl("https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=600")
                                .categoria(tortas)
                                .build(),
                        Producto.builder()
                                .nombre("Caja Bombones Artesanales 16 un.")
                                .descripcion("Surtido de bombones rellenos con manjar de campo, avellana chilena y calafate.")
                                .precio(12500)
                                .stock(25)
                                .oferta(15)
                                .imagenUrl("https://images.unsplash.com/photo-1549007994-cb92caebd54b?w=600")
                                .categoria(chocolates)
                                .build(),
                        Producto.builder()
                                .nombre("Mermelada Casera de Rosa Mosqueta 450g")
                                .descripcion("Receta tradicional patagónica 100% natural sin preservantes.")
                                .precio(5490)
                                .stock(30)
                                .oferta(0)
                                .imagenUrl("https://images.unsplash.com/photo-1597223557154-721c1cecc4b0?w=600")
                                .categoria(mermeladas)
                                .build()
                ));
            }
        }
    }
}