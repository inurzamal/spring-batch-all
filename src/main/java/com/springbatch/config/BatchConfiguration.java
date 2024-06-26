package com.springbatch.config;

import com.springbatch.domain.*;
import com.springbatch.processor.FilterProductItemProcessor;
import com.springbatch.processor.MyProductItemProcessor;
import com.springbatch.reader.ProductNameItemReader;
import com.springbatch.repository.ProductRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.*;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	public DataSource dataSource;

	@Bean
	public ItemReader<String> itemReader() {
		List<String> productList = Arrays.asList("Product 1","Product 2","Product 3");
		return new ProductNameItemReader(productList);
	}

	@Bean
	public ItemReader<Product> flatFileItemReader() {
		FlatFileItemReader<Product> itemReader = new FlatFileItemReader<>();
		itemReader.setLinesToSkip(1);
		itemReader.setResource(new ClassPathResource("/data/Product_Details.csv"));

		DefaultLineMapper<Product> lineMapper = new DefaultLineMapper<>();

		DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
		lineTokenizer.setNames("product_id", "product_name", "product_category", "product_price");

		lineMapper.setLineTokenizer(lineTokenizer);
		lineMapper.setFieldSetMapper(new ProductFieldSetMapper());

		itemReader.setLineMapper(lineMapper);

		return itemReader;
	}

	@Bean
	public ItemReader<Product> jdbcCursorItemReader() {
		JdbcCursorItemReader<Product> itemReader = new JdbcCursorItemReader<>();
		itemReader.setDataSource(dataSource);
		itemReader.setSql("select * from product_details order by product_id");
		itemReader.setRowMapper(new ProductRowMapper());
		return itemReader;
	}

	@Bean
	public ItemReader<Product> jdbcPagingItemReader() throws Exception {
		JdbcPagingItemReader<Product> itemReader = new JdbcPagingItemReader<>();
		itemReader.setDataSource(dataSource);

		SqlPagingQueryProviderFactoryBean factory = new SqlPagingQueryProviderFactoryBean();
		factory.setDataSource(dataSource);
		factory.setSelectClause("select product_id, product_name, product_category, product_price");
		factory.setFromClause("from product_details");
		factory.setSortKey("product_id");

		itemReader.setQueryProvider(factory.getObject());
		itemReader.setRowMapper(new ProductRowMapper());
		itemReader.setPageSize(3);

		return itemReader;
	}

	@Bean
	public ItemReader<Product> repositoryItemReader() throws Exception {
		RepositoryItemReader<Product> itemReader = new RepositoryItemReader<>();
		itemReader.setRepository(productRepository);
		itemReader.setMethodName("findAll"); // Custom method name
		itemReader.setPageSize(100);

		// Setting the sorting criteria is optional
		itemReader.setSort(Collections.singletonMap("productId", Sort.Direction.ASC));

		itemReader.afterPropertiesSet(); // Needed to initialize the reader
		return itemReader;
	}

	@Bean
	public ItemWriter<Product> flatFileItemWriter() {
		FlatFileItemWriter<Product> itemWriter = new FlatFileItemWriter<>();
		itemWriter.setResource(new FileSystemResource("output/Product_Details_Output.csv"));

		DelimitedLineAggregator<Product> lineAggregator = new DelimitedLineAggregator<>();
		lineAggregator.setDelimiter(",");

		BeanWrapperFieldExtractor<Product> fieldExtractor = new BeanWrapperFieldExtractor<>();
		fieldExtractor.setNames(new String[] { "productId", "productName", "productCategory", "productPrice" });

		lineAggregator.setFieldExtractor(fieldExtractor);

		itemWriter.setLineAggregator(lineAggregator);
		return itemWriter;
	}

	@Bean
	public JdbcBatchItemWriter<Product> jdbcBatchItemWriter() {
		JdbcBatchItemWriter<Product> itemWriter = new JdbcBatchItemWriter<>();
		itemWriter.setDataSource(dataSource);
		itemWriter.setSql("insert into product_details_output values (:productId, :productName, :productCategory, :productPrice)");
		itemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider());
		return itemWriter;
	}

	@Bean
	public JdbcBatchItemWriter<OSProduct> jdbcBatchItemWriters() {
		JdbcBatchItemWriter<OSProduct> itemWriter = new JdbcBatchItemWriter<>();
		itemWriter.setDataSource(dataSource);
		itemWriter.setSql("insert into os_product_details values (:productId, :productName, :productCategory, :productPrice, :taxPercent, :sku, :shippingRate)");
		itemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider());
		return itemWriter;
	}

	@Bean
	public ItemWriter<Product> jpaItemWriter() {
		return items -> productRepository.saveAll(items);
	}

	@Bean
	public ItemProcessor<Product, Product> filterProductItemProcessor() {
		return new FilterProductItemProcessor();
	}

	@Bean
	public ItemProcessor<Product, OSProduct> myProductItemProcessor() {
		return new MyProductItemProcessor();
	}

	@Bean
	public ValidatingItemProcessor<Product> validateProductItemProcessors() {
		ValidatingItemProcessor<Product> validatingItemProcessor = new ValidatingItemProcessor<>(new ProductValidator());
		validatingItemProcessor.setFilter(true);
		return validatingItemProcessor;
	}

	@Bean
	public BeanValidatingItemProcessor<Product> validateProductItemProcessor() {
		BeanValidatingItemProcessor<Product> beanValidatingItemProcessor = new BeanValidatingItemProcessor<>();
		beanValidatingItemProcessor.setFilter(true);
		return beanValidatingItemProcessor;
	}

	@Bean
	public Step stepOne(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) throws Exception {
		return new StepBuilder("chunkBasedStep1", jobRepository)
				.<Product,Product>chunk(3, platformTransactionManager)
				.reader(repositoryItemReader())
				.processor(validateProductItemProcessor())
				.writer(jpaItemWriter())
				.build();
	}

	@Bean
	public Job firstJob(JobRepository jobRepository, Step stepOne) {
		return new JobBuilder("jobOne", jobRepository)
				.start(stepOne)
				.build();
	}
}
