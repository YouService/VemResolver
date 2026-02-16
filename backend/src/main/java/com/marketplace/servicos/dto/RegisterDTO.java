package com.marketplace.servicos.dto;

import com.marketplace.servicos.model.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterDTO {
    
    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;
    
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$", 
             message = "A senha deve conter pelo menos uma letra maiúscula, uma minúscula e um número")
    private String senha;
    
    @NotNull(message = "Tipo de usuário é obrigatório")
    private TipoUsuario tipo;

    private String telefone;

    private String cpf;

    private String rg;

    private String orgaoExpedidor;

    private String dataExpedicaoRg;

    private String cep;

    private String endereco;

    private String cidade;

    private String estado;

    private String numero;

    private String complemento;

    public RegisterDTO(@NotBlank(message = "Nome é obrigatório") String nome,
            @NotBlank(message = "Email é obrigatório") @Email(message = "Email inválido") String email,
            @NotBlank(message = "Senha é obrigatória") @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres") @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$", message = "A senha deve conter pelo menos uma letra maiúscula, uma minúscula e um número") String senha,
            @NotNull(message = "Tipo de usuário é obrigatório") TipoUsuario tipo, String telefone, String cpf,
            String rg, String orgaoExpedidor, String dataExpedicaoRg, String cep, String endereco, String cidade,
            String estado, String numero, String complemento) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.tipo = tipo;
        this.telefone = telefone;
        this.cpf = cpf;
        this.rg = rg;
        this.orgaoExpedidor = orgaoExpedidor;
        this.dataExpedicaoRg = dataExpedicaoRg;
        this.cep = cep;
        this.endereco = endereco;
        this.cidade = cidade;
        this.estado = estado;
        this.numero = numero;
        this.complemento = complemento;
    }

    public RegisterDTO() {
    }   

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public void setTipo(TipoUsuario tipo) {
        this.tipo = tipo;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getRg() {
        return rg;
    }

    public void setRg(String rg) {
        this.rg = rg;
    }

    public String getOrgaoExpedidor() {
        return orgaoExpedidor;
    }

    public void setOrgaoExpedidor(String orgaoExpedidor) {
        this.orgaoExpedidor = orgaoExpedidor;
    }

    public String getDataExpedicaoRg() {
        return dataExpedicaoRg;
    }

    public void setDataExpedicaoRg(String dataExpedicaoRg) {
        this.dataExpedicaoRg = dataExpedicaoRg;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    @Override
    public String toString() {
        return "RegisterDTO [getNome()=" + getNome()  + 
                         ", getEmail()=" + getEmail() + 
                         ", getSenha()=" + getSenha() + 
                         ", getTipo()=" + getTipo() + 
                         ", getTelefone()=" + getTelefone() + 
                         ", getCpf()=" + getCpf() + 
                         ", getRg()=" + getRg() + 
                         ", getOrgaoExpedidor()=" + getOrgaoExpedidor() + 
                         ", getDataExpedicaoRg()=" + getDataExpedicaoRg() + 
                         ", getCep()=" + getCep() + 
                         ", getEndereco()=" + getEndereco() + 
                         ", getCidade()=" + getCidade() + 
                         ", getEstado()=" + getEstado() + 
                         ", getNumero()=" + getNumero() + 
                         ", getComplemento()=" + getComplemento() + 
                         "]";
    }

}
