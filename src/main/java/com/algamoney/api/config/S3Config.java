package com.algamoney.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.algamoney.api.config.property.AlgamoneyApiProperty;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.Tag;
import com.amazonaws.services.s3.model.lifecycle.LifecycleFilter;
import com.amazonaws.services.s3.model.lifecycle.LifecycleTagPredicate;

@Configuration
public class S3Config {
	
	@Autowired
	private AlgamoneyApiProperty property;
	
	@Bean
	public AmazonS3 amazonS3() {
		
		// Pega as credenciais
		AWSCredentials credentials = new BasicAWSCredentials(property.getS3().getAccessKeyId(), property.getS3().getSecretAccessKey());
		
		// Instancia um novo objeto amazonS3
		AmazonS3 amazonS3 = AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials))
				.build();
		
		// Se não existir cria um novo bucket
		// Lembrando que pode ter arquivos temporarios no nosso bucket, que seram excluídos depois de um dia, se tiver a tag expirar
		if (!amazonS3.doesBucketExistV2(property.getS3().getBucket())) {
			
			// Cria um novo bucket
			amazonS3.createBucket(new CreateBucketRequest(property.getS3().getBucket()));
			
			BucketLifecycleConfiguration.Rule regraExpiracao = new BucketLifecycleConfiguration.Rule()
					.withId("Regra de expiração de arquivos temporários")
					.withFilter(new LifecycleFilter(new LifecycleTagPredicate(new Tag("expirar","true"))))
					.withExpirationInDays(1)
					.withStatus(BucketLifecycleConfiguration.ENABLED);

			// Criando o objeto de configuração
			BucketLifecycleConfiguration configuration = new BucketLifecycleConfiguration()
					.withRules(regraExpiracao);
			
			// Associando a regra com o bucket criado
			amazonS3.setBucketLifecycleConfiguration(property.getS3().getBucket(), configuration);
		}
		
		return amazonS3;
	}
	

}
