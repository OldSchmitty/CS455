import javax.security.auth.login.Configuration;
import javax.xml.soap.Text;
import java.nio.file.Path;

class Main {

    public static void main(String[] args) {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "word count");
        job.setJarByClass(WordCountJob.class);
        job.setMapperClass(WordCountMapper.class);
        job.setCombinerClass(WordCountReducer.class);
        job.setReducerClass(WordCountReducer.class);
        job.setMapOutputKeyClass(Text.class);
        //Mandatory if Mapper’s output Key and Value are different than Reducer’s job.
        setMapOutputValueClass(IntWritable.class);
        //Mandatory if Mapper’s output Key and Value are different than Reducer’s
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        job.setInputFormat(....class);
        //Default InputFormat is TextInputFormat
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        //Must be deleted before starting job
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}