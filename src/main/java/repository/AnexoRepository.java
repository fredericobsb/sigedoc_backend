package repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import models.Anexo;

import java.util.List;

@Repository
public interface AnexoRepository extends JpaRepository<Anexo, Long> {
	
	@Query(value = "select u.id, u.matricula, u.username, a.nome_documento, a.data_inclusao, a.data_atualizacao from Users u inner join anexo a on u.matricula = a.matricula", nativeQuery=true)
	public List<Object[]> listarUsuariosComDocumentos();
}

