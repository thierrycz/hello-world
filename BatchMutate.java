package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

public class BatchMutate {

//	final long MAX = 110000;
	static List<Row> batch = new ArrayList<Row>();
	static Connection connection;
	static String hbaseTable;
	static String keyfile;
	static long chunksize;
	static char mode;

	public BatchMutate(String source) throws IOException {

		processFile();
	}

	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
		init();
//		new TestBufferedReader("d:\\hadoop\\suivi_07_key_sample.job");
//		new BatchMutate("d:\\hadoop\\suivi_10_keyfab.job");

		lectureParametres(args);
//		System.exit(0);
		new BatchMutate(keyfile);

		long endTime = System.currentTimeMillis();
		System.out.printf("duration %d seconds", (endTime - startTime) / 1000);
	}

	static void init() throws IOException {

		Configuration conf = HBaseConfiguration.create();
		connection = ConnectionFactory.createConnection(conf);
	}

	private void processFile() throws IOException {

//		lecture du fichier et écriture dans hbase 

		Table tbl = connection.getTable(TableName.valueOf(hbaseTable));

		String fam = "colfam1";
		String qual = "typeMessage";
		String val = "cReM_";

		System.out.println(mode + " en cours");
		batch.clear();

		try {
			String ligne;
			BufferedReader fichier = new BufferedReader(new FileReader(keyfile));
			long i = 1;

			while ((ligne = fichier.readLine()) != null) {

				if (mode == 'd') {
					Delete d = new Delete(Bytes.toBytes(ligne));
					batch.add(d);
				} else {
					Put put = new Put(Bytes.toBytes(ligne));
					put.addColumn(Bytes.toBytes(fam), Bytes.toBytes(qual), Bytes.toBytes(val + i));
					batch.add(put);
				}

				if ((i % chunksize) == 0) {
					System.out.println(String.format("%d %s ", i, ligne));
//					System.out.println("écriture dans hbase");
//					System.out.println("reset de la liste et on repart");
					processListe(batch, tbl);
					batch.clear();
				}

				i++;

//				if (i > MAX)
//					break;
			}

			// process le restant

			processListe(batch, tbl);

			fichier.close();

			tbl.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// recoit une liste de Row puis la balance dans hbase
	public void processListe(List<Row> l, Table t) {

		System.out.println("batch size = " + l.size());

		Object[] results = new Object[l.size()];
		try {
			t.batch(batch, results);
		} catch (Exception e) {
			System.err.println("Error: " + e);
		}
	}

	public static void lectureParametres(String[] param) {
		System.out.println("Liste des paramètres ");

		if (param.length >= 4) {
			
			hbaseTable = param[0];
			keyfile = param[1];
			mode = param[2].charAt(0);
			chunksize = Long.parseLong(param[3]);
			
			System.out.printf("%s %s %c %d \n", hbaseTable, keyfile, mode, chunksize);
			File f = new File(keyfile);

			if (f.exists() == true) {
				System.out.println("le fichier " + keyfile + " existe, il fait " + f.length() / 1024 + " Ko.");
			} else {
				System.out.println("Erreur: fichier " + keyfile + " absent, exiting ");
				System.exit(1);
			}

		} else {
			System.out.println("Usage : prog hbase_table keyfilename delete | create chunck_size");
			System.exit(1);
		}
	}

}