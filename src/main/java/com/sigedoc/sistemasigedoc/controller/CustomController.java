package com.sigedoc.sistemasigedoc.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags="Customs")
@RestController
@RequestMapping(value="/customs")
public class CustomController {

    @PostMapping
    @ApiOperation(value="Criar custom")
    public String custom() {
        return "custom criado";
    }
    
    @ApiOperation(value="Listar custom")
    @GetMapping
    public String listar() {
    	return "customs listados";
    }
    
    @ApiOperation(value="Update custom")
    @PutMapping("/{id}")
    public String updateCustom() {
    	return "custom alterado";
    }
    
    @ApiOperation(value="Delete custom")
    @DeleteMapping("/{id}")
    public String deletarCustom() {
    	return "Custom deletado";
    }
}

