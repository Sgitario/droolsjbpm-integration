package org.drools.container.spring.namespace;

import static org.drools.container.spring.namespace.DefinitionParserHelper.emptyAttributeCheck;

import java.util.List;

import org.drools.builder.DecisionTableInputType;
import org.drools.builder.JaxbConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.builder.conf.impl.DecisionTableConfigurationImpl;
import org.drools.container.spring.beans.DroolsResourceAdapter;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ResourceDefinitionParser extends AbstractBeanDefinitionParser {

    private static final String WORKSHEET_NAME_ATTRIBUTE = "worksheet-name";
    private static final String INPUT_TYPE_ATTRIBUTE     = "input-type";
    private static final String TYPE_ATTRIBUTE           = "type";
    private static final String SOURCE_ATTRIBUTE         = "source";
    private static final String REF                      = "ref";


    @SuppressWarnings("unchecked")
    @Override
    protected AbstractBeanDefinition parseInternal(Element element,
                                                   ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition( DroolsResourceAdapter.class );
        
        if ( StringUtils.hasText( element.getAttribute( REF ) )) {
            String ref = element.getAttribute( REF);
            emptyAttributeCheck( element.getLocalName(),
                                 REF,
                                 ref );
            return (AbstractBeanDefinition) parserContext.getRegistry().getBeanDefinition( ref );            
        }

        String source = element.getAttribute( SOURCE_ATTRIBUTE );
        emptyAttributeCheck( element.getLocalName(),
                             SOURCE_ATTRIBUTE,
                             source );
        factory.addPropertyValue( "resource",
                                  source );

        String type = element.getAttribute( TYPE_ATTRIBUTE );

        String resourceType = type == null || type.length() == 0 ? ResourceType.DRL.getName() : type;

        factory.addPropertyValue( "resourceType",
                                  resourceType );

        if ( "xsd".equals( resourceType.toLowerCase() ) ) {
            XsdParser.parse( element, parserContext, factory );
        } else if ( "dtable".equals( resourceType.toLowerCase() ) ) {
            List<Element> childElements = DomUtils.getChildElementsByTagName( element,
                                                                              "decisiontable-conf" );
            if ( !childElements.isEmpty() ) {
                Element conf = childElements.get( 0 );
                DecisionTableConfigurationImpl dtableConf = new DecisionTableConfigurationImpl();

                String inputType = conf.getAttribute( INPUT_TYPE_ATTRIBUTE );
                emptyAttributeCheck( conf.getLocalName(),
                                     INPUT_TYPE_ATTRIBUTE,
                                     inputType );
                dtableConf.setInputType( DecisionTableInputType.valueOf( inputType ) );

                String worksheetName = conf.getAttribute( WORKSHEET_NAME_ATTRIBUTE );
                emptyAttributeCheck( conf.getLocalName(),
                                     WORKSHEET_NAME_ATTRIBUTE,
                                     worksheetName );
                dtableConf.setWorksheetName( worksheetName );

                factory.addPropertyValue( "resourceConfiguration",
                                          dtableConf );
            }
        }

        return factory.getBeanDefinition();
    }

    public void emptyAttributeCheck(final String element,
                                    final String attributeName,
                                    final String attribute) {
        if ( attribute == null || attribute.trim().equals( "" ) ) {
            throw new IllegalArgumentException( "<" + element + "> requires a '" + attributeName + "' attribute" );
        }
    }
}
