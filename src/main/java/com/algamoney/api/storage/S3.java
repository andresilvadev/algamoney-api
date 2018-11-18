package com.algamoney.api.storage;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.algamoney.api.config.property.AlgamoneyApiProperty;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ObjectTagging;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.Tag;

@Component
public class S3 {

	private static final Logger logger = LoggerFactory.getLogger(S3.class);
	
	@Autowired
	private AlgamoneyApiProperty property;
	
	@Autowired
	private AmazonS3 amazonS3;
	
	
	public String salvarTemporariamente(MultipartFile arquivo) {
		// Cria uma instancia do accessControlList para dizer que o objeto pode ser lido
		AccessControlList accessControlList = new AccessControlList();
		accessControlList.grantPermission(GroupGrantee.AllUsers, Permission.Read);
		
		// Criamos os metadados, com o content type e o tamanho do arquivo
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentType(arquivo.getContentType());
		objectMetadata.setContentLength(arquivo.getSize());
		
		// Geramos um nome único
		String nomeUnico = gerarNomeUnico(arquivo.getOriginalFilename());
		
		// Criamos a requisição para incluir o objeto
		try {
			PutObjectRequest putObjectRequest = new PutObjectRequest(
					property.getS3().getBucket(),
					nomeUnico,
					arquivo.getInputStream(), 
					objectMetadata)
					.withAccessControlList(accessControlList);
			
			// Colocamos que este objeto vai ter a tag expirar
			putObjectRequest.setTagging(new ObjectTagging(
					Arrays.asList(new Tag("expirar", "true"))));
			
			// Enviamos para o S3
			amazonS3.putObject(putObjectRequest);
			
			// Debug simples
			if (logger.isDebugEnabled()) {
				logger.debug("Arquivo enviado com sucesso para o S3.", arquivo.getOriginalFilename());
			}
			
			// Devolve o nome único
			return nomeUnico;
			
		} catch (IOException e) {
			throw new RuntimeException("Problemas ao tentar enviar o arquivo para o S3.", e);			
		}
	}

	public String configurarUrl(String objeto) {
		return "http://" + property.getS3().getBucket() + ".s3.amazonaws.com/" + objeto;
	}

	private String gerarNomeUnico(String originalFilename) {
		return UUID.randomUUID().toString() + "_" + originalFilename;
	}
}
