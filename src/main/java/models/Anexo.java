package models;

import java.sql.Blob;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;


@JsonIgnoreProperties({"anexo"})
@Entity
public class Anexo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "NOME_DOCUMENTO")
    private String nomeDocumento;

    @Column(name = "DATA_INCLUSAO")
    private Date dataInclusao;

    @Column(name = "DATA_ATUALIZACAO")
    private Date dataAtualizacao;

    @Column(name = "ANEXO")
    private Blob anexo;

    @Column(name = "MATRICULA")
    private String matricula;

    @Column(name = "ATIVO")
    private Boolean ativo;
    
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNomeDocumento() {
        return nomeDocumento;
    }

    public void setNomeDocumento(String nomeDocumento) {
        this.nomeDocumento = nomeDocumento;
    }

    public Date getDataInclusao() {
        return dataInclusao;
    }

    public void setDataInclusao(Date dataInclusao) {
        this.dataInclusao = dataInclusao;
    }

    public Date getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(Date dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public Blob getAnexo() {
        return anexo;
    }

    public void setAnexo(Blob anexo) {
        this.anexo = anexo;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}  
}