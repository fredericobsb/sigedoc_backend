package com.sigedoc.sistemasigedoc.controller;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.sql.rowset.serial.SerialBlob;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.sigedoc.sistemasigedoc.payload.request.LoginRequest;
import com.sigedoc.sistemasigedoc.payload.request.SignupRequest;
import com.sigedoc.sistemasigedoc.payload.response.JwtResponse;
import com.sigedoc.sistemasigedoc.payload.response.MessageResponse;
import com.sigedoc.sistemasigedoc.security.jwt.JwtUtils;
import com.sigedoc.sistemasigedoc.security.services.UserDetailsImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import models.Anexo;
import models.CorpoDoAnexoDTO;
import models.ERole;
import models.Role;
import models.User;
import models.UsuarioDocumentoDto;
import repository.AnexoRepository;
import repository.RoleRepository;
import repository.UserRepository;

@Api(tags="Usuários")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  UserRepository userRepository;
  
  @Autowired
  AnexoRepository anexoRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  JwtUtils jwtUtils;

  @PostMapping("/signin")
  @ApiOperation(value="Login de Usuário")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtils.generateJwtToken(authentication);
    
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();    
    List<String> roles = userDetails.getAuthorities().stream()
        .map(item -> item.getAuthority())
        .collect(Collectors.toList());

    return ResponseEntity.ok(new JwtResponse(jwt, 
                         userDetails.getId(), 
                         userDetails.getUsername(), 
                         userDetails.getEmail(), 
                         roles));
  }

  @PostMapping("/signup")
  @ApiOperation(value="Cadastro de Usuário")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      return ResponseEntity
          .badRequest()
          .body(new MessageResponse("Error: Username is already taken!"));
    }

    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      return ResponseEntity
          .badRequest()
          .body(new MessageResponse("Error: Email is already in use!"));
    }

    // Create new user's account
    User user = new User(signUpRequest.getUsername(), 
               signUpRequest.getEmail(),
               encoder.encode(signUpRequest.getPassword()),
               signUpRequest.getMatricula());

    Set<String> strRoles = signUpRequest.getRole();
    Set<Role> roles = new HashSet<>();

    if (strRoles == null) {
      Role userRole = roleRepository.findByName(ERole.ROLE_USER)
          .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
      roles.add(userRole);
    } else {
      strRoles.forEach(role -> {
        switch (role) {
        case "admin":
          Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(adminRole);

          break;
        case "mod":
          Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(modRole);

          break;
        default:
          Role userRole = roleRepository.findByName(ERole.ROLE_USER)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(userRole);
        }
      });
    }

    user.setRoles(roles);
    userRepository.save(user);

    return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
  }
  
  @RequestMapping(path="/anexo", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
  public  ResponseEntity<Anexo> Post(@RequestPart  MultipartFile file, 
		  @RequestPart("nomeDocumento") String nomeDocumento, 
		  @RequestPart("matricula")String matricula,
		  @RequestPart("user_id") String user_id) throws IOException
  {
	  Anexo anexo = new Anexo();
	  Blob blob = null;
	  try {
		  blob = new SerialBlob(file.getBytes());
	  }catch(SQLException e) {
		  e.printStackTrace();
	  }
	  anexo.setAnexo(blob);
	  anexo.setMatricula(matricula);
	  anexo.setAtivo(true);
	  anexo.setDataInclusao(new Date());
	  anexo.setNomeDocumento(nomeDocumento);
	  anexo.setUser(new User());
	  try {
		  anexo.getUser().setId(Long.parseLong(user_id));
		  anexoRepository.save(anexo);
		  return new ResponseEntity<Anexo>(anexo, HttpStatus.OK);
	  }catch(Exception e) {
		  return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	  }
  }
  
  @RequestMapping(value = "/anexo/{id}", method = RequestMethod.GET)
  public ResponseEntity<CorpoDoAnexoDTO> GetById(@PathVariable(value = "id") long id)
  {
      Optional<Anexo> anexo = anexoRepository.findById(id);
      if(anexo.isPresent()) {
    	  CorpoDoAnexoDTO corpoDoAnexoDTO = new CorpoDoAnexoDTO();
    	  corpoDoAnexoDTO.setId(String.valueOf(anexo.get().getId()));
    	  //blob to array de bytes
    	  int myBlobLength = 0;
    	  byte[] meuBlobEmByte = null;
    	  try {
    		  myBlobLength = (int) anexo.get().getAnexo().length();
        	  meuBlobEmByte = anexo.get().getAnexo().getBytes(1, myBlobLength);
    	  }catch(SQLException e) {
    		  
    	  }
    	  String base64EncodedImageBytes = Base64.getEncoder().encodeToString(meuBlobEmByte);
    	  corpoDoAnexoDTO.setAnexo(base64EncodedImageBytes);
    	  //nome do documento unico
    	  SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
    	  String dataEHoraUnica = sdf.format(new Date());
    	  corpoDoAnexoDTO.setNomeDocumento(anexo.get().getNomeDocumento() + "_" + dataEHoraUnica);
    	  return new ResponseEntity<CorpoDoAnexoDTO>(corpoDoAnexoDTO, HttpStatus.OK);
      } 
      else
          return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }
  
  @RequestMapping(value = "/exclusaoLogica/{id}", method = RequestMethod.POST)
  public ResponseEntity<Anexo> exclusaoLogica(@RequestPart("anexo_id") String anexo_id)
  {
      Optional<Anexo> anexo = anexoRepository.findById(Long.parseLong(anexo_id));
      if(anexo.isPresent()) {
    	  anexo.get().setDataAtualizacao(new Date());
    	  Anexo anexoAlterado = anexoRepository.save(anexo.get());
    	  return new ResponseEntity<Anexo>(anexoAlterado, HttpStatus.OK);
      }  
      else
          return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }
  
  @RequestMapping(value = "/anexo/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<Object> Delete(@PathVariable(value = "id") long id)
  {
      Optional<Anexo> anexo = anexoRepository.findById(id);
      if(anexo.isPresent()){
          anexoRepository.delete(anexo.get());
          return new ResponseEntity<>(HttpStatus.OK);
      }
      else
          return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }
  
  @PreAuthorize("hasRole('USER')")
  @RequestMapping(value = "/usuariosComDocumentos", method = RequestMethod.GET)
  public ResponseEntity<List<UsuarioDocumentoDto>> getAll(){
	  List<Object[]>listaObjetos = anexoRepository.listarUsuariosComDocumentos();
	  List<UsuarioDocumentoDto>listaUsuarioComDocumentos = new ArrayList<>();
	  UsuarioDocumentoDto dto = null;
	  if(listaObjetos.size() > 0) {
		  for(Object [] o : listaObjetos) {
			  dto = new UsuarioDocumentoDto();
			  dto.setId(Long.parseLong(o[0].toString()));
			  dto.setMatricula(o[1].toString());
			  dto.setUsuario(o[2].toString());
			  dto.setNomeDocumento(o[3].toString());
			  dto.setDataInclusao(o[4].toString());
			  if(o[5] != null) {
				  dto.setDataAtualizacao(o[5].toString());
			  }
			  listaUsuarioComDocumentos.add(dto);
		  }
		  return new ResponseEntity<List<UsuarioDocumentoDto>>(listaUsuarioComDocumentos, HttpStatus.OK);
	  }else {
		  return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	  }
  }
  
  @PreAuthorize("hasRole('USER')")
  @RequestMapping(value = "/users", method = RequestMethod.GET)
  public ResponseEntity<List<User>> getUsers(){
	  List<User>listaUsuarios = userRepository.findAll();
	  if(listaUsuarios.size() > 0) {
		  return new ResponseEntity<List<User>>(listaUsuarios, HttpStatus.OK);
	  }else {
		  return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	  }
  }
}