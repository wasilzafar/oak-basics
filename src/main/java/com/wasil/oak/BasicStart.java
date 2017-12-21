package com.wasil.oak;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.sql.DataSource;

import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.jcr.Jcr;
import org.apache.jackrabbit.oak.plugins.document.DocumentMK;
import org.apache.jackrabbit.oak.plugins.document.DocumentNodeStore;
import org.apache.jackrabbit.oak.plugins.document.rdb.RDBDataSourceFactory;
import org.apache.jackrabbit.oak.plugins.document.rdb.RDBOptions;
import org.apache.jackrabbit.oak.plugins.memory.MemoryNodeStore;
import org.apache.jackrabbit.oak.segment.SegmentNodeStore;
import org.apache.jackrabbit.oak.segment.SegmentNodeStoreBuilders;
import org.apache.jackrabbit.oak.segment.file.FileStore;
import org.apache.jackrabbit.oak.segment.file.FileStoreBuilder;
import org.apache.jackrabbit.oak.segment.file.InvalidFileStoreVersionException;
import org.apache.jackrabbit.oak.spi.blob.BlobStore;
import org.apache.jackrabbit.oak.spi.blob.FileBlobStore;
import com.mongodb.DB;
import com.mongodb.MongoClient;

public class BasicStart {
	static Repository repository;
	static Session session;
	static FileStore fs;
	static DocumentNodeStore ns;

	public static void main(String[] args) {
		int key;
		System.out.println("Enter the type of repository(1-4) :");
		System.out.println("1: Memory.");
		System.out.println("2: Segment TAR.");
		System.out.println("3: Mongo.");
		System.out.println("4: RDB.");
		key = new Scanner(System.in).nextInt();
		switch (key) {
		case 1:
			getMemoryNSRepository();
		try {
			sessionSave();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}finally {
			session.logout();
		}			
			break;
		case 2:
			getSegmentNSRepository();
			try {
				sessionSave();
			} catch (RepositoryException e) {
				e.printStackTrace();
			}finally {
				session.logout();
			    fs.close();
			}			
			break;
		case 3:
			getDocumentMongoNSRepository();
			try {
				sessionSave();
			} catch (RepositoryException e) {
				e.printStackTrace();
			}finally {
				session.logout();
			    ns.dispose();
			}
			break;
		case 4:
			getDocmentRDBNSRepository();
			try {
				sessionSave();
			} catch (RepositoryException e) {
				e.printStackTrace();
			}finally {
				session.logout();
			    ns.dispose();
			}
			break;
		default:
			System.out.println("Wrong choice!");
			break;
		}		
	}

	public static final Repository getSegmentNSRepository() {
		if (repository == null) {
			try {
				fs = FileStoreBuilder.fileStoreBuilder(new File("TARMK-repository"))
						.withBlobStore((BlobStore) new FileBlobStore("TARMK-repository/blob")).build();
				SegmentNodeStore ns = SegmentNodeStoreBuilders.builder(fs).build();
				repository = new Jcr(new Oak(ns)).createRepository();
			} catch (InvalidFileStoreVersionException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return repository;
	}

	public static final Repository getMemoryNSRepository() {
		if (repository == null) {
			repository = new Jcr(new Oak(new MemoryNodeStore())).createRepository();// Repository repo = new Jcr(new
																					// Oak()).createRepository();
		}
		return repository;
	}

	public static final Repository getDocumentMongoNSRepository() {
		@SuppressWarnings({ "deprecation", "resource" })
		DB db = (DB) new MongoClient("127.0.0.1", 27017).getDB("MongoMK");
		ns = new DocumentMK.Builder().setBlobStore((BlobStore) new FileBlobStore("MongoMK-repository/blob"))
				.setMongoDB(db).getNodeStore();
		return repository = new Jcr(new Oak(ns)).createRepository();
	}

	public static final Repository getDocmentRDBNSRepository() {
		DataSource ds = RDBDataSourceFactory.forJdbcUrl("jdbc:mysql://localhost:3306/rdbmkoak", "root", "password");
		RDBOptions options = new RDBOptions().tablePrefix("RDBMK_").dropTablesOnClose(false);
		ns = new DocumentMK.Builder().setBlobStore((BlobStore) new FileBlobStore("RDBMK-repository/blob"))
				.setRDBConnection(ds, options).getNodeStore();
		return repository = new Jcr(new Oak(ns)).createRepository();
	}

	public static void sessionSave() throws RepositoryException {
		session = repository.login(getAdminCredentials());
		Node root = session.getRootNode();
		if (root.hasNode("hello")) {
			Node hello = root.getNode("hello");
			long count = hello.getProperty("count").getLong();
			hello.setProperty("count", count + 1);
			try {
				hello.setProperty("file", new FileInputStream("C:\\Users\\zafar\\Desktop\\LargeFile1.zip"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			System.out.println("found the hello node, count = " + count);
		} else {
			System.out.println("creating the hello node");
			root.addNode("hello").setProperty("count", 1);
			Node hello = root.getNode("hello");
			try {
				hello.setProperty("file", new FileInputStream("C:\\Users\\zafar\\Desktop\\LargeFile2.zip"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		session.save();
	}

	protected static SimpleCredentials getAdminCredentials() {
		return new SimpleCredentials("admin", "admin".toCharArray());
	}

}
