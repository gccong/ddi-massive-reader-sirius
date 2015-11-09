package uk.ac.ebi.ddi.massive.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ddi.massive.extws.massive.model.DatasetDetail;
import uk.ac.ebi.ddi.massive.extws.massive.model.Publication;
import uk.ac.ebi.ddi.massive.model.*;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Reader using SAX the XML file
 * @author ypriverol
 */
public class ReaderMassiveProject {

    private static final Logger logger = LoggerFactory.getLogger(ReaderMassiveProject.class);

    private static final String MASSIVE = "massive";

    private static final String MASSIVE_LINK = "http://massive.ucsd.edu/ProteoSAFe/result.jsp?task=";

    /**
     * This method read the PX summary file and return a project structure to be use by the
     * EBE exporter.
     * @return Project object model
     */
    public static Project readProject(DatasetDetail dataset, List<Specie> species) throws Exception {

        Project proj = new Project();

        proj.setAccession(dataset.getId());
        
        proj.setRepositoryName(MASSIVE);

        proj.setTitle(dataset.getTitle());

        proj.setProjectDescription(dataset.getDescription());

        proj.setInstrument(transformInstrument(dataset.getInstrument()));

        proj.setSpecies(species);

        proj.setSubmitter(transformSubmitter(dataset.getPrincipalInvestigator()));

        proj.setDatasetLink(dataset.getUrl());

        proj.setSubmissionDate(transformDate(dataset.getCreated()));

        proj.setDataProcessingProtocol(transformDataProcessing(dataset));

        proj.setProjectTags(transformKeywords(dataset.getKeywords()));

        proj.setOmicsType(transformToOmicsType(dataset));

        proj.setHash(dataset.getTask());

        proj.setDatasetLink(MASSIVE_LINK+dataset.getTask()+"&view=advanced_view");

        proj.setReferences(transformReferences(dataset.getPublications()));

        return proj;
    }

    /**
     * Convert citations from Massive to References
     * @param publications
     * @return
     */
    private static List<Reference> transformReferences(Publication[] publications) {
        List<Reference> references = new ArrayList<>();
        if(publications != null && publications.length > 0){
            for(Publication publication:publications){
                if(publication != null){
                    String citation = (publication.getTitle() != null)?publication.getTitle():"";
                    citation = (publication.getAuthors() != null)? citation + ". " + publication.getAuthors():citation;
                    citation = (publication.getCitation() != null)?citation + ". " + publication.getCitation():citation;
                    references.add(new Reference(publication.getPubmedId(), (citation.isEmpty())?null:citation));
                }
            }
        }
        return references;

    }

    private static List<String> transformToOmicsType(DatasetDetail dataset) {
        List<String> types = new ArrayList<>();
        types.add(Constants.PROTEOMICS_TYPE);
        if(dataset != null && dataset.getKeywords() != null){
            if(dataset.getKeywords().toLowerCase().contains(Constants.METABOLOMICS_PATTERN) ||
                    dataset.getKeywords().toLowerCase().contains(Constants.METABOLITE_PATTERN)){
                types.add(Constants.METABOLOMICS_TYPE);
                types.remove(Constants.PROTEOMICS_TYPE);
            }
        }
        return types;
    }

    /**
     * This function transform alist of keywords from Massive to a list of omicsDI tags
     * also remove the whitespeace and the end of each word.
     * @param keywords String with keywords from massive
     * @return List of String Tags
     */
    private static List<String> transformKeywords(String keywords) {
        List<String> resultKeys = new ArrayList<>();
        if(keywords != null && !keywords.isEmpty()){
            String[] instrumentArray = keywords.split(Constants.MASSIVE_SEPARATOR);
            for(String key: instrumentArray)
                resultKeys.add(key.trim());

        }
        return resultKeys;
    }

    private static List<String> transformDataProcessing(DatasetDetail dataset) {
        return null;
    }

    private static Submitter transformSubmitter(String principalInvestigator) {
        return null;

    }

    /**
     * Convert the String date to a Date.
     * @param created String date
     * @return Date
     */
    private static Date transformDate(String created) {
        if(created != null && !created.isEmpty()){
            DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            try {
                return df1.parse(created);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * This function converts an Instrument String to a list of different Instruments, also it trim every
     * Instrument to remove the corresponding whitespace at the end of the String.
     * @param instrument
     * @return
     */
    private static Set<Instrument> transformInstrument(String instrument) {
        Set<Instrument> instruments = new HashSet<>();
        if(instrument != null && !instrument.isEmpty()){
            String[] instrumentArray = instrument.split(Constants.MASSIVE_SEPARATOR);
            for(String instrumentTrimed: instrumentArray)
                instruments.add(new Instrument(null, instrumentTrimed.trim()));
        }
        return instruments;
    }




}
