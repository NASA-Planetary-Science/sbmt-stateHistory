package edu.jhuapl.sbmt.stateHistory.config;

import java.util.Date;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.sbmt.core.body.BodyViewConfig;
import edu.jhuapl.sbmt.core.config.BaseFeatureConfigIO;
import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;

import edu.jhuapl.ses.jsqrl.api.Key;
import edu.jhuapl.ses.jsqrl.api.Metadata;
import edu.jhuapl.ses.jsqrl.api.Version;
import edu.jhuapl.ses.jsqrl.impl.SettableMetadata;

public class StateHistoryConfigIO extends BaseFeatureConfigIO //  BaseInstrumentConfigIO implements MetadataManager
{
	final Key<SpiceInfo> spiceInfoKey = Key.of("spiceInfo");
	final Key<Boolean> hasStateHistoryKey = Key.of("hasStateHistory");
	final Key<Long> stateHistoryStartKey = Key.of("stateHistoryStart");
	final Key<Long> stateHistoryEndKey = Key.of("stateHistoryEnd");
	final Key<String> timeHistoryFileKey = Key.of("timeHistoryFile");

//	private StateHistoryConfig c = new StateHistoryConfig();
	private String metadataVersion = "1.0";
	private ViewConfig viewConfig;

	public StateHistoryConfigIO()
	{
		
	}
	
	public StateHistoryConfigIO(String metadataVersion, ViewConfig viewConfig)
	{
		this.metadataVersion = metadataVersion;
		this.viewConfig = viewConfig;
	}

	@Override
	public void retrieve(Metadata configMetadata)
	{
		featureConfig = new StateHistoryConfig((BodyViewConfig)viewConfig);
		StateHistoryConfig c = (StateHistoryConfig)featureConfig;
		c.hasStateHistory = read(hasStateHistoryKey, configMetadata);
		c.spiceInfo = read(spiceInfoKey, configMetadata);
		
		Long stateHistoryStartDate = read(stateHistoryStartKey, configMetadata);
		Long stateHistoryEndDate = read(stateHistoryEndKey, configMetadata);
		if (stateHistoryStartDate != null)
		{
			c.stateHistoryStartDate = new Date(stateHistoryStartDate);
			c.stateHistoryEndDate = new Date(stateHistoryEndDate);
		}
		
		
		c.timeHistoryFile = read(timeHistoryFileKey, configMetadata);
	}

	@Override
	public Metadata store()
	{
		SettableMetadata result = SettableMetadata.of(Version.of(metadataVersion));
		storeConfig(result);
//		SettableMetadata configMetadata = storeConfig(viewConfig);
//		Key<SettableMetadata> metadata = Key.of(viewConfig.getUniqueName());
//		result.put(metadata, configMetadata);
		return result;
	}

	private SettableMetadata storeConfig(SettableMetadata configMetadata)
	{
		StateHistoryConfig c = (StateHistoryConfig)featureConfig;
//		SettableMetadata configMetadata = SettableMetadata.of(Version.of(metadataVersion));

		write(hasStateHistoryKey, c.hasStateHistory, configMetadata);
		write(spiceInfoKey, c.spiceInfo, configMetadata);
		writeDate(stateHistoryStartKey, c.stateHistoryStartDate, configMetadata);
		writeDate(stateHistoryEndKey, c.stateHistoryEndDate, configMetadata);
		write(timeHistoryFileKey, c.timeHistoryFile, configMetadata);

		return configMetadata;
	}

}
