package co.edu.unbosque.reventacero.security;

import java.util.Optional;

import org.springframework.security.core.userdetails
        .UserDetails;

import org.springframework.security.core.userdetails
        .UserDetailsService;

import org.springframework.security.core.userdetails
        .UsernameNotFoundException;

import org.springframework.stereotype.Service;

import co.edu.unbosque.reventacero.entity.Usuario;

import co.edu.unbosque.reventacero.repository.UsuarioRepository;

/**
 * Servicio encargado de cargar
 * usuarios para Spring Security.
 */
@Service
public class UserDetailsServiceImpl
        implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Constructor.
     */
    public UserDetailsServiceImpl(
            UsuarioRepository usuarioRepository) {

        this.usuarioRepository =
                usuarioRepository;
    }

    /**
     * Busca un usuario por correo.
     */
    @Override
    public UserDetails loadUserByUsername(
            String correo)

            throws UsernameNotFoundException {

        Optional<Usuario> usuario =
                usuarioRepository
                        .findByCorreo(correo);

        if (usuario.isPresent()) {

            return usuario.get();
        }

        throw new UsernameNotFoundException(

                "Usuario no encontrado con correo: "
                        + correo);
    }
}