package functionalanalysisplugin;

import executablembse.PortBasedConnector;
import generalhelpers.GeneralHelpers;
import generalhelpers.Logger;
import generalhelpers.StatechartHelpers;
import generalhelpers.StereotypeAndPropertySettings;
import generalhelpers.TraceabilityHelper;
import generalhelpers.UserInterfaceHelpers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.telelogic.rhapsody.core.*;

public class CreateIncomingEventPanel extends CopyOfCreateTracedElementPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JCheckBox m_EventActionIsNeededCheckBox;
	private JCheckBox m_CreateSendEvent;
	private JCheckBox m_CreateSendEventViaPanel;
	private JCheckBox m_AttributeCheckBox;
	private JCheckBox m_CheckOperationCheckBox;
	private String m_CheckOpName;
	private JTextField m_AttributeNameTextField;
	private JPanel m_AttributeNamePanel;
	private JLabel m_CreateAttributeLabel;
	private IRPPackage m_PackageForEvent;
	private IRPActor m_SourceActor;
	private IRPPort m_SourceActorPort;

	public static void main(String[] args) {
		launchThePanel();
	}

	public CreateIncomingEventPanel(
			String theAppID ){
		
		super( theAppID );
		
		Logger.writeLine("CreateIncomingEventPanel invoked");
		
		IRPClass theBuildingBlock = 
				m_ElementContext.getBuildingBlock();

		if( theBuildingBlock == null ){
			
			buildUnableToRunDialog( 
					"Sorry, this helper is unable to run this command because \n" +
					"there was no execution context or block found in the model. \n " +
					"You need to add the relevant package structure first." );
			
		} else { // theBuildingBlock != null 

			m_PackageForEvent = m_ElementContext.getPkgThatOwnsEventsAndInterfaces();

			List<IRPModelElement> theCandidateActors = 
					getNonElapsedTimeActorsRelatedTo( theBuildingBlock );

			if( !theCandidateActors.isEmpty() ){

				final IRPModelElement theActor = 
						GeneralHelpers.launchDialogToSelectElement(
								theCandidateActors, "Select Actor", true );

				if( theActor != null && theActor instanceof IRPActor ){

					IRPClass theBlock = m_ElementContext.getBlockUnderDev(
							"Which Block do you want to add the Event to?" );

					if( theBlock != null ){

						final IRPClass theChosenBlock = theBlock;

						IRPPort thePort = GeneralHelpers.getPortThatConnects(
								(IRPActor)theActor, 
								(IRPClassifier)theChosenBlock,
								theBuildingBlock );

						if( thePort == null ){

							UserInterfaceHelpers.showWarningDialog(
									"Unable to find a port that connects " + Logger.elementInfo(theActor) + " to the " + 
											Logger.elementInfo( theChosenBlock ) + ". \n" +
											"You may want to add the necessary ports and connector to the IBD under " + 
											Logger.elementInfo( theBuildingBlock ) + " \nbefore trying this." );

							theBuildingBlock.highLightElement();

						} else { // Port != null

							m_SourceActorPort = thePort;
							m_SourceActor = (IRPActor) theActor;
							
							buildContent();
							commonPanelSetup();
						}
					}
				}
			}
		}

//		m_SourceActor = theSourceActor;
//		m_SourceActorPort = theSourceActorPort;
//		m_PackageForEvent = thePackageForEvent;
	}
/*
	public CopyOfCreateIncomingEventPanel(
			String theAppID,
			IRPGraphElement forSourceGraphElement, 
			IRPClassifier onTargetBlock,
			Set<IRPRequirement> withReqtsAlsoAdded,
			IRPPackage thePackageForEvent ){

		super( theAppID );//super( theAppID, forSourceGraphElement, withReqtsAlsoAdded, onTargetBlock, onTargetBlock.getProject() );

		m_SourceActor = null;
		m_SourceActorPort = null;
		m_PackageForEvent = thePackageForEvent;

		String theSourceText = GeneralHelpers.getActionTextFrom( m_SourceGraphElement.getModelObject() );	

		if( theSourceText.isEmpty() ){
			theSourceText = m_Tbd;
		}

		Logger.writeLine("CreateIncomingEventPanel constructor (2) called with text '" + theSourceText + "'");

		String theProposedName = GeneralHelpers.determineUniqueNameBasedOn( 
				GeneralHelpers.toMethodName("req" + GeneralHelpers.capitalize( theSourceText ) ), 
				"Event", 
				onTargetBlock.getProject());								

		Logger.writeLine("The proposed name is '" + theProposedName + "'");

		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		m_EventActionIsNeededCheckBox = new JCheckBox("Populate on diagram?");
		setupPopulateCheckbox( m_EventActionIsNeededCheckBox );

		JPanel thePageStartPanel = new JPanel();
		thePageStartPanel.setLayout( new BoxLayout( thePageStartPanel, BoxLayout.X_AXIS ) );
		thePageStartPanel.add( createChosenNamePanelWith( "Create an event called:  ", theProposedName ) );
		thePageStartPanel.add( m_EventActionIsNeededCheckBox );

		add( thePageStartPanel, BorderLayout.PAGE_START );

		JPanel theCenterPanel = new JPanel();
		theCenterPanel.setLayout( new BoxLayout( theCenterPanel, BoxLayout.Y_AXIS ) );

		m_RequirementsPanel.setAlignmentX(LEFT_ALIGNMENT);
		theCenterPanel.add( m_RequirementsPanel );

		m_CreateSendEvent = new JCheckBox();
		m_CreateSendEvent.setSelected(false);
		m_CreateSendEvent.setVisible(false);
		theCenterPanel.add( m_CreateSendEvent );

		m_AttributeCheckBox = new JCheckBox("Add a value argument to change a corresponding attribute on the block");

		m_AttributeCheckBox.addActionListener( new ActionListener() {

			public void actionPerformed(ActionEvent actionEvent) {

				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();

				boolean selected = abstractButton.getModel().isSelected();

				m_CheckOperationCheckBox.setEnabled(selected);
				m_CreateAttributeLabel.setEnabled(selected);
				m_AttributeNameTextField.setEnabled(selected);			        
				m_CheckOperationCheckBox.setSelected(selected);
				setupSendEventViaPanelCheckbox( m_SourceActor );	        
				updateNames();

			}} );

		theCenterPanel.add( m_AttributeCheckBox );

		m_AttributeNamePanel = createAttributeNamePanel( 
				GeneralHelpers.determineUniqueNameBasedOn( 
						GeneralHelpers.toMethodName( theSourceText ), 
						"Attribute", 
						onTargetBlock ) );

		m_AttributeNamePanel.setAlignmentX(LEFT_ALIGNMENT);

		theCenterPanel.add( m_AttributeNamePanel );

		m_CheckOperationCheckBox = new JCheckBox();
		m_CheckOperationCheckBox.setSelected(false);
		m_CheckOperationCheckBox.setEnabled(false);
		theCenterPanel.add( m_CheckOperationCheckBox );

		m_CreateSendEventViaPanel = new JCheckBox();
		setupSendEventViaPanelCheckbox( m_SourceActor );

		theCenterPanel.add( m_CreateSendEventViaPanel );	

		add( theCenterPanel, BorderLayout.WEST );

		commonPanelSetup();
	}

	public CopyOfCreateIncomingEventPanel(
			String theAppID,
			IRPModelElement forModelElement, 
			IRPClassifier onTargetBlock,
			Set<IRPRequirement> withReqtsAlsoAdded,
			IRPActor theSourceActor, 
			IRPPort theSourceActorPort,
			IRPPackage thePackageForEvent ){

		super(theAppID, forModelElement, withReqtsAlsoAdded, onTargetBlock, onTargetBlock.getProject() );

		m_SourceActor = theSourceActor;
		m_SourceActorPort = theSourceActorPort;
		m_PackageForEvent = thePackageForEvent;

		String theSourceText = m_Tbd;

		if( forModelElement instanceof IRPAttribute ){
			theSourceText = forModelElement.getName();
		}

		Logger.writeLine("CreateIncomingEventPanel constructor (3) called with text '" + theSourceText + "'");

		String theProposedName = determineBestEventName( onTargetBlock, theSourceText );									

		Logger.writeLine("The proposed name is '" + theProposedName + "'");

		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		JPanel thePageStartPanel = new JPanel();
		thePageStartPanel.setLayout( new BoxLayout( thePageStartPanel, BoxLayout.X_AXIS ) );
		thePageStartPanel.add( createChosenNamePanelWith( "Create an event called:  ", theProposedName ) );

		m_EventActionIsNeededCheckBox = new JCheckBox("Populate on diagram?");

		if( forModelElement instanceof IRPAttribute  || 
				StereotypeAndPropertySettings.getIsPopulateOptionHidden( m_SourceGraphElementDiagram ) ){

			m_EventActionIsNeededCheckBox.setSelected( false );
			m_EventActionIsNeededCheckBox.setVisible( false );

			JLabel filler = new JLabel("                                                  ");

			thePageStartPanel.add( filler );

		} else {			

			setupPopulateCheckbox( m_EventActionIsNeededCheckBox );			
			thePageStartPanel.add( m_EventActionIsNeededCheckBox );
		}

		add( thePageStartPanel, BorderLayout.PAGE_START );

		JPanel theCenterPanel = new JPanel();
		theCenterPanel.setLayout( new BoxLayout( theCenterPanel, BoxLayout.Y_AXIS ) );

		m_RequirementsPanel.setAlignmentX(LEFT_ALIGNMENT);
		theCenterPanel.add( m_RequirementsPanel );

		m_CreateSendEvent = new JCheckBox();
		m_CreateSendEvent.setSelected( m_SourceActor != null );
		m_CreateSendEvent.setEnabled( m_SourceActor != null );
		m_CreateSendEvent.setVisible( m_SourceActor != null );

		theCenterPanel.add( m_CreateSendEvent );

		m_AttributeCheckBox = new JCheckBox("Add a value argument to change a corresponding attribute on the block");

		if( forModelElement instanceof IRPAttribute ){

			m_AttributeCheckBox.setSelected(true);
			m_AttributeCheckBox.setEnabled(false);
		} else {
			m_AttributeCheckBox.setSelected(false);
			m_AttributeCheckBox.setEnabled(true);

			m_AttributeCheckBox.addActionListener( new ActionListener() {

				public void actionPerformed(ActionEvent actionEvent) {

					AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();

					boolean selected = abstractButton.getModel().isSelected();

					m_CheckOperationCheckBox.setEnabled(selected);
					m_CreateAttributeLabel.setEnabled(selected);
					m_AttributeNameTextField.setEnabled(selected);

					setupSendEventViaPanelCheckbox( m_SourceActor ); 			        	

					m_CheckOperationCheckBox.setSelected(selected);

					updateNames();

				}} );
		}

		theCenterPanel.add( m_AttributeCheckBox );

		if ( forModelElement instanceof IRPAttribute ){
			m_AttributeNamePanel = new JPanel();
			m_AttributeNamePanel.setLayout( new BoxLayout( m_AttributeNamePanel, BoxLayout.X_AXIS ) );	

			m_CreateAttributeLabel = new JLabel("Use the attribute called:  ");
			m_CreateAttributeLabel.setEnabled(false);

			m_AttributeNamePanel.add( m_CreateAttributeLabel );

			m_AttributeNameTextField = new JTextField( theSourceText );
			m_AttributeNameTextField.setMaximumSize( new Dimension( 300,20 ) );

			if (forModelElement instanceof IRPAttribute){
				m_AttributeNameTextField.setEnabled(false);
			} else {
				m_AttributeNameTextField.setEnabled(true);
			}

			m_AttributeNamePanel.add( m_AttributeNameTextField );
			m_AttributeNamePanel.setAlignmentX( LEFT_ALIGNMENT );

		} else {

			m_AttributeNamePanel = createAttributeNamePanel( 
					GeneralHelpers.determineUniqueNameBasedOn( 
							GeneralHelpers.toMethodName( theSourceText ), 
							"Attribute", 
							onTargetBlock ) );

			m_AttributeNamePanel.setAlignmentX( LEFT_ALIGNMENT );
		}

		theCenterPanel.add( m_AttributeNamePanel );

		m_CreateSendEventViaPanel = new JCheckBox();
		setupSendEventViaPanelCheckbox( m_SourceActor );

		theCenterPanel.add( m_CreateSendEventViaPanel );

		m_CheckOperationCheckBox = new JCheckBox();
		m_CheckOperationCheckBox.setSelected(false);
		m_CheckOperationCheckBox.setVisible(false);

		theCenterPanel.add( m_CheckOperationCheckBox );

		add( theCenterPanel, BorderLayout.WEST );

		commonPanelSetup();
	}*/
	
	private void buildContent() {
		String theSourceText = GeneralHelpers.getActionTextFrom( 
				m_ElementContext.getSelectedEl() );	

		if( theSourceText.isEmpty() ){
			theSourceText = m_Tbd;
		}

		Logger.writeLine("CreateIncomingEventPanel constructor (1) called with text '" + theSourceText + "'");

		String theProposedName = determineBestEventName( 
				m_ElementContext.getChosenBlock(), 
				theSourceText );									

		Logger.writeLine("The proposed name is '" + theProposedName + "'");

		setLayout( new BorderLayout(10,10) );
		setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		m_EventActionIsNeededCheckBox = new JCheckBox("Populate on diagram?");
		setupPopulateCheckbox( m_EventActionIsNeededCheckBox );

		JPanel thePageStartPanel = new JPanel();
		thePageStartPanel.setLayout( new BoxLayout( thePageStartPanel, BoxLayout.X_AXIS ) );
		thePageStartPanel.add( createChosenNamePanelWith( "Create an event called:  ", theProposedName ) );
		thePageStartPanel.add( m_EventActionIsNeededCheckBox );

		add( thePageStartPanel, BorderLayout.PAGE_START );

		JPanel theCenterPanel = new JPanel();
		theCenterPanel.setLayout( new BoxLayout( theCenterPanel, BoxLayout.Y_AXIS ) );

		m_RequirementsPanel.setAlignmentX(LEFT_ALIGNMENT);
		theCenterPanel.add( m_RequirementsPanel );

		m_CreateSendEvent = new JCheckBox();
		m_CreateSendEvent.setSelected( m_SourceActor != null );
		theCenterPanel.add( m_CreateSendEvent );

		m_AttributeCheckBox = new JCheckBox(
				"Add a value argument to change a corresponding attribute on the block");

		m_AttributeCheckBox.addActionListener( new ActionListener() {

			public void actionPerformed(ActionEvent actionEvent) {

				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();

				boolean selected = abstractButton.getModel().isSelected();

				m_CheckOperationCheckBox.setEnabled(selected);
				m_CreateAttributeLabel.setEnabled(selected);
				m_AttributeNameTextField.setEnabled(selected);

				setupSendEventViaPanelCheckbox( m_SourceActor );

				m_CheckOperationCheckBox.setSelected(selected);

				if( selected==false ){
					m_CreateSendEventViaPanel.setSelected(false);			        	
				}

				updateNames();

			}} );

		theCenterPanel.add( m_AttributeCheckBox );

		m_AttributeNamePanel = createAttributeNamePanel( 
				GeneralHelpers.determineUniqueNameBasedOn( 
						GeneralHelpers.toMethodName( theSourceText, 40 ), 
						"Attribute", 
						m_ElementContext.getChosenBlock() ) );

		m_AttributeNamePanel.setAlignmentX( LEFT_ALIGNMENT );

		theCenterPanel.add( m_AttributeNamePanel );

		m_CheckOperationCheckBox = new JCheckBox();
		m_CheckOperationCheckBox.setSelected(false);
		m_CheckOperationCheckBox.setEnabled(false);
		theCenterPanel.add( m_CheckOperationCheckBox );

		m_CreateSendEventViaPanel = new JCheckBox();
		setupSendEventViaPanelCheckbox( m_SourceActor );

		theCenterPanel.add( m_CreateSendEventViaPanel );	

		add( theCenterPanel, BorderLayout.WEST );
	}

	private void setupSendEventViaPanelCheckbox(
			IRPActor theSourceActor ) {

		boolean isAttributeForEvent = 
				m_AttributeCheckBox.isSelected();

		boolean isSendEventViaPanelOptionEnabled = 
				StereotypeAndPropertySettings.getIsSendEventViaPanelOptionEnabled(
						m_ElementContext.getSelectedEl() );

		boolean isSendEventViaPanelWantedByDefault = 
				StereotypeAndPropertySettings.getIsSendEventViaPanelWantedByDefault(
						m_ElementContext.getSelectedEl() );

		m_CreateSendEventViaPanel.setSelected( 
				isAttributeForEvent && 
				isSendEventViaPanelWantedByDefault &&
				theSourceActor != null );

		m_CreateSendEventViaPanel.setEnabled( 
				isAttributeForEvent && 
				isSendEventViaPanelOptionEnabled &&
				theSourceActor != null );

		m_CreateSendEventViaPanel.setVisible( 
				isSendEventViaPanelOptionEnabled &&
				theSourceActor != null );
	}

	private void commonPanelSetup(){
		add( createOKCancelPanel(), BorderLayout.PAGE_END );

		m_ChosenNameTextField.getDocument().addDocumentListener(
				new DocumentListener() {

					@Override
					public void changedUpdate(DocumentEvent arg0) {
						updateNames();					
					}

					@Override
					public void insertUpdate(DocumentEvent arg0) {
						updateNames();
					}

					@Override
					public void removeUpdate(DocumentEvent arg0) {
						updateNames();
					}	
				});

		updateNames();
	}

	public static void launchThePanel(){

		final String theAppID = 
				UserInterfaceHelpers.getAppIDIfSingleRhpRunningAndWarnUserIfNot();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				JFrame.setDefaultLookAndFeelDecorated( true );

				JFrame frame = new JFrame( "Create an incoming event " );

				frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

				CreateIncomingEventPanel thePanel = 
						new CreateIncomingEventPanel(
								theAppID );

				frame.setContentPane( thePanel );

				frame.pack();
				frame.setLocationRelativeTo( null );
				frame.setVisible( true );
			}
		});
	}

	private String determineBestEventName(
			IRPClassifier onTargetBlock,
			final String theSourceText ){

		String theProposedName = null;

		if( m_SourceActor != null ){
			String[] splitActorName = m_SourceActor.getName().split("_");
			String theActorName = splitActorName[0];
			String theSourceMinusActor = theSourceText.replaceFirst( "^" + theActorName, "" );

			theProposedName = GeneralHelpers.determineUniqueNameBasedOn( 
					GeneralHelpers.toMethodName(
							"req" + theActorName + GeneralHelpers.capitalize(theSourceMinusActor), 40 ), 
					"Event", 
					onTargetBlock.getProject());
		} else {
			theProposedName = GeneralHelpers.determineUniqueNameBasedOn( 
					GeneralHelpers.toMethodName( 
							"req" + GeneralHelpers.capitalize( theSourceText ), 40 ), 
					"Event", 
					onTargetBlock.getProject());
		}

		return theProposedName;
	}

	private void updateNames(){

		if( m_AttributeCheckBox.isSelected() ){
			m_CreateSendEvent.setText(
					"Add a '" + determineSendEventNameFor( m_ChosenNameTextField.getText() ) 
					+ "' event with 'value' argument to the actor for webify/test creation usage");
		} else {
			m_CreateSendEvent.setText(
					"Add a '" + determineSendEventNameFor( m_ChosenNameTextField.getText() ) 
					+ "' event to the actor");
		}

		m_CreateSendEventViaPanel.setText(
				"Add a '" + determineSendEventViaPanelNameFor( m_ChosenNameTextField.getText() ) 
				+ "' event and '" + m_AttributeNameTextField.getText() + "' attribute to the actor to ease panel creation");

		m_CheckOpName = GeneralHelpers.determineBestCheckOperationNameFor(
				m_ElementContext.getChosenBlock(), 
				m_AttributeNameTextField.getText(),
				40 );

		m_CheckOperationCheckBox.setText(
				"Add a '" + m_CheckOpName + "' operation to the block that returns the attribute value");
	}

	private JPanel createAttributeNamePanel(
			String theProposedName ){

		JPanel thePanel = new JPanel();
		thePanel.setLayout( new BoxLayout(thePanel, BoxLayout.X_AXIS ) );	

		m_CreateAttributeLabel = new JLabel("Create an attribute called:  ");
		m_CreateAttributeLabel.setEnabled(false);
		thePanel.add( m_CreateAttributeLabel );

		m_AttributeNameTextField = new JTextField( theProposedName );
		m_AttributeNameTextField.setMaximumSize( new Dimension( 300,20 ) );
		m_AttributeNameTextField.setEnabled(false);

		m_AttributeNameTextField.getDocument().addDocumentListener(
				new DocumentListener() {

					@Override
					public void changedUpdate(DocumentEvent arg0) {
						updateNames();					
					}

					@Override
					public void insertUpdate(DocumentEvent arg0) {
						updateNames();
					}

					@Override
					public void removeUpdate(DocumentEvent arg0) {
						updateNames();
					}	
				});

		thePanel.add( m_AttributeNameTextField );

		return thePanel;
	}

	private void removeDependenciesToRequirementsFrom(
			IRPModelElement theEl ){

		@SuppressWarnings("unchecked")
		List<IRPDependency> theDependencies = theEl.getDependencies().toList();

		for (Iterator<IRPDependency> iter = theDependencies.listIterator(); iter.hasNext(); ) {

			IRPDependency theDependency = iter.next();

			IRPModelElement theDependsOn = theDependency.getDependsOn();

			if( theDependsOn != null && 
					theDependsOn instanceof IRPRequirement ){

				Logger.writeLine("Deleting " + Logger.elementInfo( theDependency ) );
				theDependency.deleteFromProject();
			}
		}
	}

	private IRPModelElement createTestBenchSendFor(
			IRPEvent theEvent, 
			IRPActor onTheActor, 
			String withSendEventName){

		IRPEvent sendEvent = null;

		IRPStatechart theStatechart = onTheActor.getStatechart();

		if (theStatechart != null){

			IRPState theReadyState = 
					StatechartHelpers.getStateCalled(
							"Ready", 
							theStatechart, 
							onTheActor );

			if (theReadyState != null){

				Logger.writeLine("Creating event called " + withSendEventName 
						+ " on actor called " + onTheActor.getName());

				sendEvent = (IRPEvent) theEvent.clone(withSendEventName, onTheActor.getOwner());

				removeDependenciesToRequirementsFrom( sendEvent );

				Logger.writeLine("The state called " + theReadyState.getFullPathName() + 
						" is owned by " + theReadyState.getOwner().getFullPathName());

				IRPTransition theTransition = theReadyState.addInternalTransition( sendEvent );

				String actionText = "OPORT( " + m_SourceActorPort.getName() + 
						" )->GEN(" + theEvent.getName() + "(";

				@SuppressWarnings("unchecked")
				List<IRPArgument> theArguments = theEvent.getArguments().toList();

				for (Iterator<IRPArgument> iter = theArguments.iterator(); iter.hasNext();) {
					IRPArgument theArgument = (IRPArgument) iter.next();
					actionText += "params->" + theArgument.getName();

					if (iter.hasNext()) actionText += ",";
				}

				actionText += "));";

				theTransition.setItsAction(actionText);

				sendEvent.addStereotype("Web Managed", "Event");

			} else {
				Logger.writeLine("Error in createTestBenchSendFor, the actor called " 
						+ onTheActor.getFullPathName() + "'s statechart does not have a Ready state");
			}
		} else {
			Logger.writeLine("Unable to proceed as actor called " 
					+ onTheActor.getFullPathName() + " does not have a statechart");
		}

		return sendEvent;
	}

	private IRPModelElement createTestBenchSendViaPanelFor(
			IRPEvent theEvent,
			IRPAttribute theAttribute, 
			IRPActor onTheActor, 
			String withSendEventName){

		IRPEvent sendEvent = null;

		IRPStatechart theStatechart = onTheActor.getStatechart();

		if (theStatechart != null){

			IRPState theReadyState = 
					StatechartHelpers.getStateCalled(
							"Ready", 
							theStatechart, 
							onTheActor );

			if (theReadyState != null){

				Logger.writeLine("Creating event called " + withSendEventName 
						+ " on actor called " + onTheActor.getName());

				sendEvent = (IRPEvent) onTheActor.getOwner().addNewAggr("Event", withSendEventName);

				Logger.writeLine("The state called " + theReadyState.getFullPathName() + 
						" is owned by " + theReadyState.getOwner().getFullPathName());

				IRPTransition theTransition = theReadyState.addInternalTransition( sendEvent );

				IRPModelElement existingEl = 
						GeneralHelpers.findElementWithMetaClassAndName(
								"Attribute", theAttribute.getName(), onTheActor );

				IRPAttribute theAttributeOwnedByActor;

				if( existingEl != null && existingEl instanceof IRPAttribute ){

					theAttributeOwnedByActor = (IRPAttribute)existingEl;
				} else {
					theAttributeOwnedByActor = 
							(IRPAttribute) theAttribute.clone(
									theAttribute.getName(), onTheActor );
				}

				String actionText = "OPORT( " + m_SourceActorPort.getName() + " )->GEN( " + 
						theEvent.getName() + "( " + theAttributeOwnedByActor.getName() + " ) );" ;

				theTransition.setItsAction(actionText);

			} else {
				Logger.writeLine("Error in createTestBenchSendViaPanelFor, the actor called " 
						+ onTheActor.getFullPathName() + "'s statechart does not have a Ready state");
			}
		} else {
			Logger.writeLine("Unable to proceed as actor called " 
					+ onTheActor.getFullPathName() + " does not have a statechart");
		}

		return sendEvent;
	}

	private void addSetAttributeTransitionToMonitoringStateFor(
			IRPAttribute theAttribute, 
			String triggeredByTheEventName, 
			IRPClassifier theOwnerOfStatechart) {

		IRPStatechart theStatechart = theOwnerOfStatechart.getStatechart();

		IRPState theMonitoringState = 
				StatechartHelpers.getStateCalled("MonitoringConditions", theStatechart, theOwnerOfStatechart);

		if (theMonitoringState != null){
			Logger.writeLine(theMonitoringState, "found");

			IRPTransition theTransition = theMonitoringState.addTransition(theMonitoringState);

			theTransition.setItsTrigger(triggeredByTheEventName);
			theTransition.setItsAction("set" + GeneralHelpers.capitalize(theAttribute.getName()) + "(params->value);");

			Logger.writeLine(theTransition, "was added");	

			IRPGraphElement theGraphEl = 
					StatechartHelpers.findGraphEl(
							theOwnerOfStatechart, 
							"MonitoringConditions" );

			if (theGraphEl != null){
				IRPDiagram theGraphElDiagram = theGraphEl.getDiagram();
				Logger.writeLine(theGraphElDiagram, "related to " 
						+ Logger.elementInfo(theGraphEl.getModelObject()) 
						+ " is the diagram for the GraphEl");

				IRPGraphNode theGraphNode = (IRPGraphNode)theGraphEl;

				GraphNodeInfo theNodeInfo = new GraphNodeInfo( theGraphNode );

				IRPGraphEdge theEdge = theGraphElDiagram.addNewEdgeForElement(
						theTransition, 
						theGraphNode, 
						theNodeInfo.getTopRightX(), 
						theNodeInfo.getMiddleY(), 
						theGraphNode, 
						theNodeInfo.getMiddleX(), 
						theNodeInfo.getBottomLeftY());

				Logger.writeLine( "Added edge to " + theEdge.getModelObject().getFullPathName() );
			} else {
				Logger.writeLine( "Error in addAnAttributeToMonitoringStateWith, " +
						"unable to find the MonitoringConditions state" );
			}

		} else {
			Logger.writeLine("Error did not find MonitoringConditions state");
		}
	}

	@Override
	protected boolean checkValidity(
			boolean isMessageEnabled) {

		String errorMessage = null;
		boolean isValid = true;

		String theChosenName = m_ChosenNameTextField.getText();
		String theAttributeName = m_AttributeNameTextField.getText();

		
		IRPClassifier theClassifier = (IRPClassifier)m_ElementContext.getChosenBlock();
		IRPModelElement theSourceModelEl = m_ElementContext.getSelectedEl();
		
		boolean isLegalName = GeneralHelpers.isLegalName( theChosenName, theClassifier );

		if( !isLegalName ){

			errorMessage = theChosenName + " is not legal as an identifier representing an executable event\n";				
			isValid = false;

		} else if( !GeneralHelpers.isElementNameUnique(
				theChosenName, 
				"Event", 
				m_PackageForEvent.getProject(), 
				1 ) ){

			errorMessage = "Unable to proceed as the event name '" + theChosenName + "' is not unique";
			isValid = false;

		} else if( m_AttributeCheckBox.isSelected() ){

			Logger.writeLine("An attribute is needed, chosen name was " + theAttributeName);			
			Logger.writeLine( theClassifier, "is the target owning element" );

			IRPStatechart theStatechart = theClassifier.getStatechart();

			if (theStatechart != null){
				IRPState theMonitoringState = 
						StatechartHelpers.getStateCalled( 
								"MonitoringConditions", 
								theStatechart, 
								theClassifier );

				if( theMonitoringState==null ){
					errorMessage = "You selected to update an attribute with an event. The Block has a statechart but for this to work the owning block needs to have a statechart with a MonitoringConditions \n" +
							"state. To do this make sure the chosen Block has a generalization to the BasePkg.TimeElapsedBlock";
					isValid = false;
					m_AttributeCheckBox.setSelected( false );
					m_CheckOperationCheckBox.setSelected( false );
				}
			}

			if( theStatechart == null ){

				errorMessage = "You selected to update an attribute with an event. The Block has no statechart. For this to work the owning block needs to have a statechart with a MonitoringConditions \n" +
						"state. To do this make sure the chosen Block has a generalization to the BasePkg.TimeElapsedBlock";
				isValid = false;	

				m_AttributeCheckBox.setSelected( false );
				m_CheckOperationCheckBox.setSelected( false );

			} else if( !GeneralHelpers.isLegalName( theAttributeName, theClassifier ) ){

				errorMessage = theChosenName + " is not legal as an identifier representing an executable attribute\n";				
				isValid = false;

			} else if ( (theSourceModelEl == null || 
					!( theSourceModelEl instanceof IRPAttribute ) ) &&
					!GeneralHelpers.isElementNameUnique(
							theAttributeName, 
							"Attribute", 
							theClassifier, 
							0 ) ){

				errorMessage = "Unable to proceed as the attribute name '" + theAttributeName + "' is not unique";
				isValid = false;
			}
			
		} else if( m_CheckOperationCheckBox.isSelected() && 
				!GeneralHelpers.isElementNameUnique(
						m_CheckOpName,
						"Operation",
						theClassifier, 
						1 ) ){

			errorMessage = "Unable to proceed as the check operation name '" + m_CheckOpName + "' is not unique";
			isValid = false;
		}

		if( isMessageEnabled && !isValid && errorMessage != null ){

			UserInterfaceHelpers.showWarningDialog( errorMessage );
		}

		return isValid;
	}

	private String determineSendEventNameFor(
			String theSourceEventName ){

		return "send_" + theSourceEventName.replaceFirst( "req", "" );
	}

	private String determineSendEventViaPanelNameFor(
			String theSourceEventName ){

		return "send_" + theSourceEventName.replaceFirst( "req","" ) + "ViaPanel";
	}

	@Override
	protected void performAction() {

		// checkValidity is assumed to return true

		IRPEvent theEvent = m_PackageForEvent.addEvent( m_ChosenNameTextField.getText() );

		IRPClassifier theClassifier = (IRPClassifier)m_ElementContext.getChosenBlock();
		IRPModelElement theSourceModelEl = m_ElementContext.getSelectedEl();

		// add value argument before cloning the event to create the test-bench send
		if( m_AttributeCheckBox.isSelected() ){
			theEvent.addArgument( "value" );
		}

		List<IRPRequirement> selectedReqtsList = 
				m_RequirementsPanel.getSelectedRequirementsList();

		addTraceabilityDependenciesTo( theEvent, selectedReqtsList );

		PortBasedConnector theExistingConnector = 
				new PortBasedConnector( 
						m_SourceActor, 
						theClassifier );

		theExistingConnector.addEvent( theEvent );

		IRPModelElement theReception = 
				theClassifier.addNewAggr( 
						"Reception", 
						m_ChosenNameTextField.getText() );

		addTraceabilityDependenciesTo( theReception, selectedReqtsList );

		if ( m_CreateSendEvent.isSelected() ) {

			String theSendEventName = determineSendEventNameFor( theEvent.getName() );

			Logger.writeLine( "Send event option was enabled, create event called " + 
					theSendEventName );

			IRPModelElement theTestbenchReception = 
					createTestBenchSendFor( 
							theEvent, 
							(IRPActor) m_SourceActor, 
							theSendEventName );

			theTestbenchReception.highLightElement();

		} else {

			Logger.writeLine( "Send event option was not enabled, so skipping this" );
			theReception.highLightElement();

			// If no send event and no actor then assume you want to webify the event directly on the block
			if( m_SourceActor == null ){
				theEvent.addStereotype( "Web Managed", "Event" );
			}
		}

		IRPAttribute theAttribute = null;

		if( m_AttributeCheckBox.isSelected() && 
				theClassifier instanceof IRPClassifier ){

			if( theSourceModelEl != null && 
					theSourceModelEl instanceof IRPAttribute ){

				theAttribute = (IRPAttribute)theSourceModelEl;

			} else {
				theAttribute = addAttributeTo(
						theClassifier, 
						m_AttributeNameTextField.getText(), 
						"0", 
						selectedReqtsList );
			}

			TraceabilityHelper.addAutoRippleDependencyIfOneDoesntExist(
					theAttribute, theReception );

			if( m_CreateSendEventViaPanel.isSelected() ){

				String theSendEventViaPanelName = 
						determineSendEventViaPanelNameFor( theEvent.getName() );

				String theSendEventName = 
						determineSendEventNameFor( theEvent.getName() );

				Logger.writeLine( "Send event option was enabled, create event called " + theSendEventName );

				IRPModelElement theTestbenchReceptionViaPanel = createTestBenchSendViaPanelFor( 
						theEvent, 
						theAttribute,
						(IRPActor) m_SourceActor, 
						theSendEventViaPanelName );

				theTestbenchReceptionViaPanel.highLightElement();
			}

			if ( m_CheckOperationCheckBox.isSelected() ){

				IRPOperation theCheckOp = 
						addCheckOperationFor( theAttribute, m_CheckOpName );
				
				addTraceabilityDependenciesTo( theCheckOp, selectedReqtsList );	
				theCheckOp.highLightElement();
			}

			addSetAttributeTransitionToMonitoringStateFor(
					theAttribute, 
					theEvent.getName(), 
					theClassifier );
		}	

		m_ElementContext.bleedColorToElementsRelatedTo( selectedReqtsList );

		if( m_EventActionIsNeededCheckBox.isSelected() ){
			populateReceiveEventActionOnDiagram( theEvent );
		}

		if( theAttribute != null ){

			// highlight the attribute so that it can be made a 
			// publish flow-port next, if needed
			theAttribute.highLightElement();
		} else {
			theEvent.highLightElement();			
		}
	}

	private void populateReceiveEventActionOnDiagram(
			IRPEvent theEvent ){
		
		IRPDiagram theSourceDiagram = m_ElementContext.getSourceDiagram();
		
		if( theSourceDiagram != null ){

			if( theSourceDiagram instanceof IRPActivityDiagram ){
				IRPActivityDiagram theAD = (IRPActivityDiagram)theSourceDiagram;

				IRPFlowchart theFlowchart = theAD.getFlowchart();

				IRPAcceptEventAction theAcceptEvent = 
						(IRPAcceptEventAction) theFlowchart.addNewAggr(
								"AcceptEventAction", theEvent.getName() );

				theAcceptEvent.setEvent( theEvent );			

				theFlowchart.addNewNodeForElement( 
						theAcceptEvent, 
						getSourceElementX(), 
						getSourceElementY(), 
						300, 
						40 );

				theAcceptEvent.highLightElement();

			} else if( theSourceDiagram instanceof IRPObjectModelDiagram ){				

				IRPObjectModelDiagram theOMD = (IRPObjectModelDiagram)theSourceDiagram;

				IRPGraphNode theEventNode = theOMD.addNewNodeForElement(
						theEvent, 
						getSourceElementX() + 50, 
						getSourceElementY() + 50, 
						300, 
						40 );	

				IRPGraphElement theSourceGraphEl = m_ElementContext.getSelectedGraphEl();
				
				if( theSourceGraphEl != null ){

					IRPCollection theGraphElsToDraw = 
							m_ElementContext.get_rhpApp().createNewCollection();

					theGraphElsToDraw.addGraphicalItem( theSourceGraphEl );
					theGraphElsToDraw.addGraphicalItem( theEventNode );

					theOMD.completeRelations( theGraphElsToDraw, 1 );
				}

				theEvent.highLightElement();

			} else {
				Logger.writeLine( "Error in populateReceiveEventActionOnDiagram, m_SourceGraphElement is not a supported diagram" );
			}

		} else {

			Logger.writeLine( "Error in populateReceiveEventActionOnDiagram, m_SourceGraphElement is null when value was expected" );
		}
	}
}

/**
 * Copyright (C) 2016-2019  MBSE Training and Consulting Limited (www.executablembse.com)

    Change history:
    #022 30-MAY-2016: Improved handling and validation of event/operation creation by adding new forms (F.J.Chadburn)
    #024 30-MAY-2016: Check box to allow user to choose whether to add the check operation (F.J.Chadburn) 
    #029 01-JUN-2016: Add Warning Dialog helper to UserInterfaceHelpers (F.J.Chadburn)
    #030 01-JUN-2016: Improve legal name checking across helpers (F.J.Chadburn)
    #032 05-JUN-2016: Populate call operation/event actions on diagram check-box added (F.J.Chadburn)
    #033 05-JUN-2016: Add support for creation of operations and events from raw requirement selection (F.J.Chadburn)
    #034 05-JUN-2016: Re-factored design to move static constructors into appropriate panel class (F.J.Chadburn)
    #040 17-JUN-2016: Extend populate event/ops to work on OMD, i.e., REQ diagrams (F.J.Chadburn)
    #042 29-JUN-2016: launchThePanel renaming to improve Panel class design consistency (F.J.Chadburn)
    #043 03-JUL-2016: Add Derive downstream reqt for CallOps, InterfaceItems and Event Actions (F.J.Chadburn)
    #044 03-JUL-2016: Minor re-factoring/code corrections (F.J.Chadburn)
    #054 13-JUL-2016: Create a nested BlockPkg package to contain the Block and events (F.J.Chadburn)
    #062 17-JUL-2016: Create InterfacesPkg and correct build issues by adding a Usage dependency (F.J.Chadburn)
    #069 20-JUL-2016: Fix population of events/ops on diagram when creating from a transition (F.J.Chadburn)
    #078 28-JUL-2016: Added isPopulateWantedByDefault tag to FunctionalAnalysisPkg to give user option (F.J.Chadburn)
    #082 09-AUG-2016: Add a check operation check box added to the create attribute dialog (F.J.Chadburn)
    #088 09-AUG-2016: Added a create send event via Panel to the incoming event dialog (F.J.Chadburn)
    #089 15-AUG-2016: Add a pull-down list to select Block when adding events/ops in white box (F.J.Chadburn)
    #090 15-AUG-2016: Fix check operation name issue introduced in fixes #083 and #084 (F.J.Chadburn)
    #093 23-AUG-2016: Added isPopulateOptionHidden tag to allow hiding of the populate check-box on dialogs (F.J.Chadburn)
    #099 14-SEP-2016: Allow event and operation creation from right-click on AD and RD diagram canvas (F.J.Chadburn)
    #117 13-NOV-2016: Get incoming and outgoing event dialogs to work without actors in the context (F.J.Chadburn)
    #125 25-NOV-2016: AutoRipple used in UpdateTracedAttributePanel to keep check and FlowPort name updated (F.J.Chadburn)
    #127 25-NOV-2016: Improved usability of ViaPanel event creation by enabling default selection via tags (F.J.Chadburn)
    #132 25-NOV-2016: Added "Unable to find a port that connects" warning to CreateIncomingEventPanel (F.J.Chadburn)
    #186 29-MAY-2017: Add context string to getBlockUnderDev to make it clearer for user when selecting (F.J.Chadburn)
    #196 05-JUN-2017: Enhanced create traced element dialogs to be context aware for blocks/parts (F.J.Chadburn)
    #197 05-JUN-2017: Fix 8.2 issue in Incoming Event panel, create ValueProperty rather than attribute (F.J.Chadburn)
    #198 05-JUN-2017: Support for adding MonitoringConditions transitions moved into shared StatechartHelpers (F.J.Chadburn)
    #199 05-JUN-2017: Improved create event panel consistency to name event Tbd if no text provided (F.J.Chadburn)
    #200 05-JUN-2017: Hide Populate on diagram check-boxes if context is not valid (F.J.Chadburn)
    #201 05-JUN-2017: Highlight attribute post-event creation to ease potential for flow-port creation (F.J.Chadburn)
    #210 04-JUL-2017: Fixed a bug stopping event creation dialog launching when populate check-box shown (F.J.Chadburn)
    #227 06-SEP-2017: Increased robustness to stop smart link panel using non new term version of <<refine>> (F.J.Chadburn)
    #252 29-MAY-2019: Implement generic features for profile/settings loading (F.J.Chadburn)
    #256 29-MAY-2019: Rewrite to Java Swing dialog launching to make thread safe between versions (F.J.Chadburn)

    This file is part of SysMLHelperPlugin.

    SysMLHelperPlugin is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SysMLHelperPlugin is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SysMLHelperPlugin.  If not, see <http://www.gnu.org/licenses/>.
 */