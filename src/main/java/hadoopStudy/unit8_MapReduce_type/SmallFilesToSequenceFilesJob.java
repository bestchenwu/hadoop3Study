package hadoopStudy.unit8_MapReduce_type;

import hadoopStudy.unit5_compress.SmallFilesToSequenceFilesMapper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


/**
 * 将若干小文件合并成顺序文件
 *
 * @author chenwu on 2020.10.5
 */
public class SmallFilesToSequenceFilesJob extends Configured implements Tool {

    @Override
    public int run(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String inputFiles = args[0];
        String output = args[1];
        Job job = Job.getInstance(conf, SmallFilesToSequenceFilesJob.class.getSimpleName());
        Path inputPath = new Path(inputFiles);
        Path outputPath = new Path(output);
        FileSystem fs = FileSystem.get(outputPath.toUri(),conf);
        //对输入路径循环累加
        FileStatus fileStatus = fs.getFileStatus(inputPath);
        if(fileStatus.isDirectory()){
            FileStatus[] fileStatuses = fs.listStatus(inputPath);
            for(FileStatus file : fileStatuses){
                WholeFileInputFormat.addInputPath(job,file.getPath());
            }
        }else{
            WholeFileInputFormat.addInputPath(job,inputPath);
        }
        //如果输出路径存在，则先递归删除
        if(fs.exists(outputPath)){
            fs.delete(outputPath,true);
        }
        SequenceFileOutputFormat.setOutputPath(job,outputPath);
        job.setJarByClass(getClass());
        //Mapper的个数由getSplits方法返回的List<InputSplit>的大小确定
        job.setMapperClass(SmallFilesToSequenceFilesMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(BytesWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(BytesWritable.class);
        job.setInputFormatClass(WholeFileInputFormat.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);
        return job.waitForCompletion(true) ? 0 : -1;
    }

    public static void main(String[] args) throws Exception {
        SmallFilesToSequenceFilesJob job = new SmallFilesToSequenceFilesJob();
        int result = ToolRunner.run(job, args);
        System.exit(result);
    }
}
