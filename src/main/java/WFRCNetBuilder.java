import com.opencsv.CSVReader;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class WFRCNetBuilder {
    private static final Logger log = Logger.getLogger(WFRCNetBuilder.class);

    Scenario sc;
    Network network;
    NetworkFactory networkFactory;
    CoordinateTransformation ct;

    public WFRCNetBuilder(String crs){
        this.sc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        sc.getConfig().global().setCoordinateSystem(crs);
        this.network = sc.getNetwork();
        this.networkFactory = network.getFactory();

    }


    public void readNodesFile(File nodesFile){
        log.info("Reading nodes file from " + nodesFile);

        try (CSVReader csvReader = new CSVReader(new FileReader(nodesFile));) {
            String[] values = null;
            String[] header = csvReader.readNext();
            while ((values = csvReader.readNext()) != null) {
                Id<Node> nodeid = Id.createNodeId(values[0]);
                Double xCoord = Double.valueOf(values[1]);
                Double yCoord = Double.valueOf(values[2]);
                Coord coord = new Coord(xCoord, yCoord);

                Node n = networkFactory.createNode(nodeid, coord);
                network.addNode(n);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void readLinksFile(File linksFile){
        log.info("Reading links file from " + linksFile);


        try (CSVReader csvReader = new CSVReader(new FileReader(linksFile))) {
           String[] values = null;
           String[] header = csvReader.readNext();
           while ((values = csvReader.readNext()) != null) {

               Id<Link> linkId = Id.createLinkId(values[0]);
               Id<Node> aNodeId  = Id.createNodeId(values[1]);
               Id<Node> bNodeId  = Id.createNodeId(values[2]);
               Node aNode = network.getNodes().get(aNodeId);
               Node bNode = network.getNodes().get(bNodeId);

               Link l = networkFactory.createLink(linkId, aNode, bNode);

               Double aadt = Double.valueOf(values[3]);
               Double length = Double.valueOf(values[4]);
               Double speed = Double.valueOf(values[5]);
               String ftype = values[6];
               Integer lanes = Integer.valueOf(values[7]);
               Double capacity = Double.valueOf(values[8]);

               l.setCapacity(capacity);
               l.setFreespeed(speed);
               l.setLength(length);
               l.setNumberOfLanes(lanes);
               l.getAttributes().putAttribute("ftype", ftype);
               l.getAttributes().putAttribute("aadt", aadt);

               network.addLink(l);
           }
        } catch (FileNotFoundException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void writeNetwork(String outPath){
        log.info("Writing network to " + outPath);

        NetworkWriter writer = new NetworkWriter(sc.getNetwork());
        writer.write(outPath);
    }

    public static void main(String[] args) {
        File linksFile = new File(args[0]);
        File nodesFile = new File(args[1]);
        String outPath = args[2];
        String crs = args[3];


        WFRCNetBuilder builder = new WFRCNetBuilder(crs);
        builder.readNodesFile(nodesFile);
        builder.readLinksFile(linksFile);
        builder.writeNetwork(outPath);

    }


}
